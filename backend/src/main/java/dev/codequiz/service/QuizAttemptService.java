package dev.codequiz.service;

import dev.codequiz.domain.*;
import dev.codequiz.domain.enums.QuizAttemptStatus;
import dev.codequiz.dto.answer.AnswerQuizDto;
import dev.codequiz.dto.question.QuestionQuizDto;
import dev.codequiz.dto.quiz.QuizAttemptDto;
import dev.codequiz.dto.quiz.QuizAttemptStartDto;
import dev.codequiz.dto.quiz.UserAnswerResultDto;
import dev.codequiz.dto.quiz.UserAnswerSubmitDto;
import dev.codequiz.exception.*;
import dev.codequiz.mapper.AnswerMapper;
import dev.codequiz.mapper.QuestionMapper;
import dev.codequiz.mapper.QuizAttemptMapper;
import dev.codequiz.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// Самый сложный сервис в проекте — управляет жизненным циклом попытки
// прохождения квиза от старта до завершения. В отличие от Category/Topic/
// Question/Answer, здесь нет отдельной таблицы "какие вопросы входят в эту
// попытку": набор вопросов вычисляется на лету как "активные вопросы темы
// минус те, на которые уже отвечено в этой попытке" — это проще, чем
// материализовать список вопросов при старте, и не требует новой таблицы.
@Service
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserAnswerSelectionRepository userAnswerSelectionRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TopicRepository topicRepository;
    private final QuizAttemptMapper quizAttemptMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository,
                              UserAnswerRepository userAnswerRepository,
                              UserAnswerSelectionRepository userAnswerSelectionRepository,
                              QuestionRepository questionRepository,
                              AnswerRepository answerRepository,
                              TopicRepository topicRepository,
                              QuizAttemptMapper quizAttemptMapper,
                              QuestionMapper questionMapper,
                              AnswerMapper answerMapper) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.userAnswerSelectionRepository = userAnswerSelectionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.topicRepository = topicRepository;
        this.quizAttemptMapper = quizAttemptMapper;
        this.questionMapper = questionMapper;
        this.answerMapper = answerMapper;
    }

    // account берётся из аутентификации (UserPrincipal в контроллере),
    // а не из тела запроса — иначе можно было бы начать попытку от чужого имени.
    @Transactional
    public QuizAttemptDto start(QuizAttemptStartDto dto, User account) {
        Topic topic = topicRepository.findById(dto.getTopicId())
                .orElseThrow(() -> new TopicNotFoundException("Тема не найдена: id=" + dto.getTopicId()));

        long availableQuestions = questionRepository.countByTopicAndActiveTrue(topic);
        if (availableQuestions < dto.getTotalQuestions()) {
            throw new NotEnoughQuestionsException(
                    "В теме недостаточно вопросов: запрошено " + dto.getTotalQuestions()
                            + ", доступно " + availableQuestions);
        }

        QuizAttempt attempt = quizAttemptMapper.toEntity(dto);
        attempt.setAccount(account);
        attempt.setTopic(topic);
        attempt.setCorrectAnswers(0);
        attempt.setScore(0);
        attempt.setStatus(QuizAttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now());

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        return quizAttemptMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public QuizAttemptDto getById(Long id, User account) {
        return quizAttemptMapper.toDto(findOwnAttemptOrThrow(id, account));
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptDto> getHistory(User account) {
        return quizAttemptRepository.findByAccountOrderByStartedAtDesc(account).stream()
                .map(quizAttemptMapper::toDto)
                .toList();
    }

    // Следующий неотвеченный вопрос темы в рамках попытки — вычисляется
    // как "все активные вопросы темы" минус "id вопросов, на которые уже
    // есть UserAnswer в этой попытке". Порядок среди оставшихся — как
    // вернула БД (без явной сортировки/рандома, чтобы не усложнять —
    // случайный порядок можно добавить позже при необходимости).
    @Transactional(readOnly = true)
    public QuestionQuizDto getNextQuestion(Long attemptId, User account) {
        QuizAttempt attempt = findOwnAttemptOrThrow(attemptId, account);
        requireInProgress(attempt);

        Set<Long> answeredQuestionIds = userAnswerRepository.findByQuizAttempt(attempt).stream()
                .map(userAnswer -> userAnswer.getQuestion().getId())
                .collect(Collectors.toSet());

        // Список ЕЩЁ НЕ отвеченных активных вопросов темы — из него случайно
        // выбирается один. Раньше выбирался просто первый по порядку из БД
        // (findFirst()) — при каждом прохождении квиза вопросы шли в одном
        // и том же порядке, что скучно и, при желании, предсказуемо для
        // пользователя. ThreadLocalRandom, а не SecureRandom — порядок
        // вопросов в квизе не секрет и не требует криптостойкой случайности
        // (в отличие, например, от кода подтверждения в AuthService).
        List<Question> unansweredQuestions = questionRepository.findByTopicAndActiveTrue(attempt.getTopic()).stream()
                .filter(question -> !answeredQuestionIds.contains(question.getId()))
                .toList();

        if (unansweredQuestions.isEmpty()) {
            throw new QuizAttemptNotInProgressException(
                    "Все вопросы попытки уже отвечены, вызовите завершение попытки");
        }

        Question nextQuestion = unansweredQuestions.get(
                ThreadLocalRandom.current().nextInt(unansweredQuestions.size()));

        List<AnswerQuizDto> answerDtos = answerRepository.findByQuestionOrderByDisplayOrderAsc(nextQuestion).stream()
                .map(answerMapper::toQuizDto)
                .toList();

        return questionMapper.toQuizDto(nextQuestion, answerDtos);
    }

    // Основной метод: принимает ответ пользователя, проверяет его на
    // корректность, сохраняет UserAnswer + UserAnswerSelection(-ы),
    // обновляет счёт попытки и, если это был последний вопрос,
    // автоматически завершает попытку (COMPLETED).
    @Transactional
    public UserAnswerResultDto submitAnswer(Long attemptId, UserAnswerSubmitDto dto, User account) {
        QuizAttempt attempt = findOwnAttemptOrThrow(attemptId, account);
        requireInProgress(attempt);

        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException("Вопрос не найден: id=" + dto.getQuestionId()));

        // Вопрос должен принадлежать теме попытки — иначе можно было бы
        // отвечать на вопросы из другой темы и засчитывать их в эту попытку.
        if (!question.getTopic().getId().equals(attempt.getTopic().getId())) {
            throw new InvalidAnswerSelectionException("Вопрос не относится к теме этой попытки");
        }

        if (userAnswerRepository.findByQuizAttemptAndQuestion(attempt, question).isPresent()) {
            throw new QuestionAlreadyAnsweredException("На этот вопрос в рамках попытки уже дан ответ");
        }

        List<Answer> questionAnswers = answerRepository.findByQuestionOrderByDisplayOrderAsc(question);
        Set<Long> validAnswerIds = questionAnswers.stream().map(Answer::getId).collect(Collectors.toSet());
        Set<Long> selectedIds = Set.copyOf(dto.getSelectedAnswerIds());

        // Все выбранные id обязаны реально принадлежать этому вопросу —
        // иначе можно было бы прислать answerId от совсем другого вопроса.
        if (!validAnswerIds.containsAll(selectedIds)) {
            throw new InvalidAnswerSelectionException("Один или несколько вариантов ответа не относятся к этому вопросу");
        }

        Set<Long> correctAnswerIds = questionAnswers.stream()
                .filter(Answer::isCorrect)
                .map(Answer::getId)
                .collect(Collectors.toSet());

        // Правильно только если выбранные варианты ТОЧНО совпадают с
        // правильными — важно для MULTIPLE_CHOICE: не хватает варианта
        // или выбран лишний — весь ответ засчитывается как неверный.
        boolean isCorrect = selectedIds.equals(correctAnswerIds);

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuizAttempt(attempt);
        userAnswer.setQuestion(question);
        userAnswer.setCorrect(isCorrect);
        userAnswer.setAnsweredAt(LocalDateTime.now());
        UserAnswer savedUserAnswer = userAnswerRepository.save(userAnswer);

        List<Answer> selectedAnswers = questionAnswers.stream()
                .filter(answer -> selectedIds.contains(answer.getId()))
                .toList();
        for (Answer selectedAnswer : selectedAnswers) {
            UserAnswerSelection selection = new UserAnswerSelection();
            selection.setUserAnswer(savedUserAnswer);
            selection.setAnswer(selectedAnswer);
            userAnswerSelectionRepository.save(selection);
        }

        updateAttemptProgress(attempt, isCorrect);

        UserAnswerResultDto result = new UserAnswerResultDto();
        result.setQuestionId(question.getId());
        result.setCorrect(isCorrect);
        result.setCorrectAnswerIds(correctAnswerIds.stream().sorted().toList());
        result.setExplanation(question.getExplanation());
        return result;
    }

    // Пользователь может сам прервать попытку, не отвечая на все вопросы —
    // например, если передумал или закрыл приложение и вернулся позже.
    @Transactional
    public QuizAttemptDto abandon(Long attemptId, User account) {
        QuizAttempt attempt = findOwnAttemptOrThrow(attemptId, account);
        requireInProgress(attempt);

        attempt.setStatus(QuizAttemptStatus.ABANDONED);
        attempt.setFinishedAt(LocalDateTime.now());

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        return quizAttemptMapper.toDto(saved);
    }

    private void updateAttemptProgress(QuizAttempt attempt, boolean lastAnswerCorrect) {
        int correctAnswers = attempt.getCorrectAnswers() + (lastAnswerCorrect ? 1 : 0);
        attempt.setCorrectAnswers(correctAnswers);
        // Целочисленный процент правильных ответов от общего числа вопросов
        // попытки — например, 7 из 10 → score = 70.
        attempt.setScore(correctAnswers * 100 / attempt.getTotalQuestions());

        long answeredCount = userAnswerRepository.findByQuizAttempt(attempt).size();
        if (answeredCount >= attempt.getTotalQuestions()) {
            attempt.setStatus(QuizAttemptStatus.COMPLETED);
            attempt.setFinishedAt(LocalDateTime.now());
        }

        quizAttemptRepository.save(attempt);
    }

    private void requireInProgress(QuizAttempt attempt) {
        if (attempt.getStatus() != QuizAttemptStatus.IN_PROGRESS) {
            throw new QuizAttemptNotInProgressException(
                    "Попытка уже завершена или прервана, статус: " + attempt.getStatus());
        }
    }

    // Единая точка проверки владения попыткой — защита от IDOR: пользователь
    // не может обратиться к попытке другого пользователя, даже зная её id.
    private QuizAttempt findOwnAttemptOrThrow(Long id, User account) {
        return quizAttemptRepository.findByIdAndAccount(id, account)
                .orElseThrow(() -> new QuizAttemptNotFoundException("Попытка не найдена: id=" + id));
    }
}