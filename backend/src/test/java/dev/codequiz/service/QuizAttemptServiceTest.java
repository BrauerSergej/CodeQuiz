package dev.codequiz.service;

import dev.codequiz.domain.*;
import dev.codequiz.domain.enums.QuizAttemptStatus;
import dev.codequiz.dto.question.QuestionQuizDto;
import dev.codequiz.dto.quiz.QuizAttemptDto;
import dev.codequiz.dto.quiz.QuizAttemptStartDto;
import dev.codequiz.dto.quiz.UserAnswerResultDto;
import dev.codequiz.dto.quiz.UserAnswerSubmitDto;
import dev.codequiz.exception.InvalidAnswerSelectionException;
import dev.codequiz.exception.NotEnoughQuestionsException;
import dev.codequiz.exception.QuestionAlreadyAnsweredException;
import dev.codequiz.exception.QuizAttemptNotFoundException;
import dev.codequiz.exception.QuizAttemptNotInProgressException;
import dev.codequiz.mapper.AnswerMapper;
import dev.codequiz.mapper.QuestionMapper;
import dev.codequiz.mapper.QuizAttemptMapper;
import dev.codequiz.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Самый сложный сервис проекта — здесь сосредоточена вся логика подсчёта
// правильности ответов и защиты от IDOR/подделанных запросов, поэтому он
// заслуживает более плотного покрытия тестами, чем простые CRUD-сервисы
// вроде CategoryService.
@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private UserAnswerRepository userAnswerRepository;
    @Mock
    private UserAnswerSelectionRepository userAnswerSelectionRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private QuizAttemptMapper quizAttemptMapper;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private AnswerMapper answerMapper;

    private QuizAttemptService quizAttemptService;

    private User account;
    private Topic topic;

    @BeforeEach
    void setUp() {
        quizAttemptService = new QuizAttemptService(quizAttemptRepository, userAnswerRepository,
                userAnswerSelectionRepository, questionRepository, answerRepository, topicRepository,
                quizAttemptMapper, questionMapper, answerMapper);

        account = new User();
        account.setId(1L);

        topic = new Topic();
        topic.setId(10L);
    }

    @Test
    void start_createsInProgressAttempt_whenEnoughQuestionsAvailable() {
        QuizAttemptStartDto startDto = new QuizAttemptStartDto();
        startDto.setTopicId(10L);
        startDto.setTotalQuestions(5);

        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));
        when(questionRepository.countByTopicAndActiveTrue(topic)).thenReturn(20L);

        QuizAttempt mappedEntity = new QuizAttempt();
        when(quizAttemptMapper.toEntity(startDto)).thenReturn(mappedEntity);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizAttemptDto expectedDto = new QuizAttemptDto();
        when(quizAttemptMapper.toDto(any(QuizAttempt.class))).thenReturn(expectedDto);

        QuizAttemptDto result = quizAttemptService.start(startDto, account);

        assertThat(result).isSameAs(expectedDto);
        assertThat(mappedEntity.getStatus()).isEqualTo(QuizAttemptStatus.IN_PROGRESS);
        assertThat(mappedEntity.getAccount()).isSameAs(account);
        assertThat(mappedEntity.getCorrectAnswers()).isZero();
        assertThat(mappedEntity.getScore()).isZero();
    }

    @Test
    void start_throwsNotEnoughQuestions_whenTopicHasFewerActiveQuestionsThanRequested() {
        QuizAttemptStartDto startDto = new QuizAttemptStartDto();
        startDto.setTopicId(10L);
        startDto.setTotalQuestions(20);

        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));
        when(questionRepository.countByTopicAndActiveTrue(topic)).thenReturn(3L);

        assertThatThrownBy(() -> quizAttemptService.start(startDto, account))
                .isInstanceOf(NotEnoughQuestionsException.class);

        verify(quizAttemptRepository, never()).save(any());
    }

    @Test
    void getById_throwsQuizAttemptNotFound_whenAttemptBelongsToAnotherAccount() {
        // findByIdAndAccount реализует защиту от IDOR прямо на уровне
        // запроса к БД — если попытка принадлежит другому пользователю,
        // репозиторий просто ничего не найдёт (Optional.empty()), как если
        // бы такой записи не существовало вовсе.
        when(quizAttemptRepository.findByIdAndAccount(99L, account)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizAttemptService.getById(99L, account))
                .isInstanceOf(QuizAttemptNotFoundException.class);
    }

    @Test
    void submitAnswer_marksCorrectAndCompletesAttempt_whenSelectedAnswerMatchesExactlyAndItIsLastQuestion() {
        QuizAttempt attempt = inProgressAttempt(1);

        Question question = new Question();
        question.setId(100L);
        question.setTopic(topic);
        question.setExplanation("Because reasons");

        Answer correctAnswer = new Answer();
        correctAnswer.setId(1000L);
        correctAnswer.setCorrect(true);
        Answer wrongAnswer = new Answer();
        wrongAnswer.setId(1001L);
        wrongAnswer.setCorrect(false);

        UserAnswerSubmitDto submitDto = new UserAnswerSubmitDto();
        submitDto.setQuestionId(100L);
        submitDto.setSelectedAnswerIds(List.of(1000L));

        when(quizAttemptRepository.findByIdAndAccount(1L, account)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(100L)).thenReturn(Optional.of(question));
        when(userAnswerRepository.findByQuizAttemptAndQuestion(attempt, question)).thenReturn(Optional.empty());
        when(answerRepository.findByQuestionOrderByDisplayOrderAsc(question))
                .thenReturn(List.of(correctAnswer, wrongAnswer));
        when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        // После сохранения этого единственного ответа findByQuizAttempt должен
        // вернуть список из 1 элемента — так updateAttemptProgress понимает,
        // что все totalQuestions (=1) уже отвечены, и завершает попытку.
        when(userAnswerRepository.findByQuizAttempt(attempt)).thenReturn(List.of(new UserAnswer()));

        UserAnswerResultDto result = quizAttemptService.submitAnswer(1L, submitDto, account);

        assertThat(result.isCorrect()).isTrue();
        assertThat(result.getCorrectAnswerIds()).containsExactly(1000L);
        assertThat(result.getExplanation()).isEqualTo("Because reasons");
        assertThat(attempt.getCorrectAnswers()).isEqualTo(1);
        assertThat(attempt.getScore()).isEqualTo(100);
        assertThat(attempt.getStatus()).isEqualTo(QuizAttemptStatus.COMPLETED);
        assertThat(attempt.getFinishedAt()).isNotNull();
    }

    @Test
    void submitAnswer_marksIncorrect_whenMultipleChoiceSelectionIsPartial() {
        // Для MULTIPLE_CHOICE выбор должен ТОЧНО совпадать с набором
        // правильных вариантов — если правильных два, а выбран только один,
        // весь ответ засчитывается как неверный (не "частично правильный").
        QuizAttempt attempt = inProgressAttempt(5);

        Question question = new Question();
        question.setId(200L);
        question.setTopic(topic);

        Answer correct1 = new Answer();
        correct1.setId(1L);
        correct1.setCorrect(true);
        Answer correct2 = new Answer();
        correct2.setId(2L);
        correct2.setCorrect(true);

        UserAnswerSubmitDto submitDto = new UserAnswerSubmitDto();
        submitDto.setQuestionId(200L);
        submitDto.setSelectedAnswerIds(List.of(1L)); // выбрал только один из двух правильных

        when(quizAttemptRepository.findByIdAndAccount(1L, account)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(200L)).thenReturn(Optional.of(question));
        when(userAnswerRepository.findByQuizAttemptAndQuestion(attempt, question)).thenReturn(Optional.empty());
        when(answerRepository.findByQuestionOrderByDisplayOrderAsc(question)).thenReturn(List.of(correct1, correct2));
        when(userAnswerRepository.save(any(UserAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userAnswerRepository.findByQuizAttempt(attempt)).thenReturn(List.of(new UserAnswer()));

        UserAnswerResultDto result = quizAttemptService.submitAnswer(1L, submitDto, account);

        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    void submitAnswer_throwsQuestionAlreadyAnswered_whenAttemptToAnswerSameQuestionTwice() {
        QuizAttempt attempt = inProgressAttempt(5);

        Question question = new Question();
        question.setId(300L);
        question.setTopic(topic);

        UserAnswerSubmitDto submitDto = new UserAnswerSubmitDto();
        submitDto.setQuestionId(300L);
        submitDto.setSelectedAnswerIds(List.of(1L));

        when(quizAttemptRepository.findByIdAndAccount(1L, account)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(300L)).thenReturn(Optional.of(question));
        when(userAnswerRepository.findByQuizAttemptAndQuestion(attempt, question))
                .thenReturn(Optional.of(new UserAnswer()));

        assertThatThrownBy(() -> quizAttemptService.submitAnswer(1L, submitDto, account))
                .isInstanceOf(QuestionAlreadyAnsweredException.class);
    }

    @Test
    void submitAnswer_throwsInvalidAnswerSelection_whenSelectedAnswerDoesNotBelongToQuestion() {
        QuizAttempt attempt = inProgressAttempt(5);

        Question question = new Question();
        question.setId(400L);
        question.setTopic(topic);

        Answer realAnswer = new Answer();
        realAnswer.setId(1L);
        realAnswer.setCorrect(true);

        UserAnswerSubmitDto submitDto = new UserAnswerSubmitDto();
        submitDto.setQuestionId(400L);
        // 999L не относится к этому вопросу — подделанный/некорректный запрос.
        submitDto.setSelectedAnswerIds(List.of(999L));

        when(quizAttemptRepository.findByIdAndAccount(1L, account)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(400L)).thenReturn(Optional.of(question));
        when(userAnswerRepository.findByQuizAttemptAndQuestion(attempt, question)).thenReturn(Optional.empty());
        when(answerRepository.findByQuestionOrderByDisplayOrderAsc(question)).thenReturn(List.of(realAnswer));

        assertThatThrownBy(() -> quizAttemptService.submitAnswer(1L, submitDto, account))
                .isInstanceOf(InvalidAnswerSelectionException.class);
    }

    @Test
    void submitAnswer_throwsInvalidAnswerSelection_whenQuestionDoesNotBelongToAttemptTopic() {
        QuizAttempt attempt = inProgressAttempt(5);

        Topic anotherTopic = new Topic();
        anotherTopic.setId(999L);

        Question questionFromOtherTopic = new Question();
        questionFromOtherTopic.setId(500L);
        questionFromOtherTopic.setTopic(anotherTopic);

        UserAnswerSubmitDto submitDto = new UserAnswerSubmitDto();
        submitDto.setQuestionId(500L);
        submitDto.setSelectedAnswerIds(List.of(1L));

        when(quizAttemptRepository.findByIdAndAccount(1L, account)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(500L)).thenReturn(Optional.of(questionFromOtherTopic));

        assertThatThrownBy(() -> quizAttemptService.submitAnswer(1L, submitDto, account))
                .isInstanceOf(InvalidAnswerSelectionException.class);
    }

    @Test
    void getNextQuestion_returnsOnlyAnUnansweredQuestion_whenSomeQuestionsAlreadyAnswered() {
        QuizAttempt attempt = inProgressAttempt(3);

        Question answered = new Question();
        answered.setId(1L);
        answered.setTopic(topic);
        Question unanswered = new Question();
        unanswered.setId(2L);
        unanswered.setTopic(topic);

        UserAnswer existingAnswer = new UserAnswer();
        existingAnswer.setQuestion(answered);

        when(quizAttemptRepository.findByIdAndAccount(1L, account)).thenReturn(Optional.of(attempt));
        when(userAnswerRepository.findByQuizAttempt(attempt)).thenReturn(List.of(existingAnswer));
        when(questionRepository.findByTopicAndActiveTrue(topic)).thenReturn(List.of(answered, unanswered));
        when(answerRepository.findByQuestionOrderByDisplayOrderAsc(unanswered)).thenReturn(List.of());
        when(questionMapper.toQuizDto(eq(unanswered), any())).thenReturn(new QuestionQuizDto());

        QuestionQuizDto result = quizAttemptService.getNextQuestion(1L, account);

        // Раз unanswered — единственный кандидат (answered уже отвечен),
        // случайный выбор всё равно должен вернуть именно его — это
        // проверяет verify ниже: toQuizDto вызван с unanswered, а не answered.
        assertThat(result).isNotNull();
        verify(questionMapper).toQuizDto(eq(unanswered), any());
    }

    @Test
    void getNextQuestion_throwsQuizAttemptNotInProgress_whenAllQuestionsAlreadyAnswered() {
        QuizAttempt attempt = inProgressAttempt(1);

        Question question = new Question();
        question.setId(1L);
        question.setTopic(topic);

        UserAnswer existingAnswer = new UserAnswer();
        existingAnswer.setQuestion(question);

        when(quizAttemptRepository.findByIdAndAccount(1L, account)).thenReturn(Optional.of(attempt));
        when(userAnswerRepository.findByQuizAttempt(attempt)).thenReturn(List.of(existingAnswer));
        when(questionRepository.findByTopicAndActiveTrue(topic)).thenReturn(List.of(question));

        assertThatThrownBy(() -> quizAttemptService.getNextQuestion(1L, account))
                .isInstanceOf(QuizAttemptNotInProgressException.class);
    }

    private QuizAttempt inProgressAttempt(int totalQuestions) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(1L);
        attempt.setAccount(account);
        attempt.setTopic(topic);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setCorrectAnswers(0);
        attempt.setScore(0);
        attempt.setStatus(QuizAttemptStatus.IN_PROGRESS);
        return attempt;
    }

}