package dev.codequiz.controller;

import dev.codequiz.dto.question.QuestionQuizDto;
import dev.codequiz.dto.quiz.QuizAttemptDto;
import dev.codequiz.dto.quiz.QuizAttemptStartDto;
import dev.codequiz.dto.quiz.UserAnswerResultDto;
import dev.codequiz.dto.quiz.UserAnswerSubmitDto;
import dev.codequiz.security.UserPrincipal;
import dev.codequiz.service.QuizAttemptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Доступен обычным USER (и ADMIN) — правило "authenticated()" уже покрыто
// дефолтным anyRequest().authenticated() в SecurityConfig, отдельного
// правила для /quiz-attempts/** не требуется.
//
// @AuthenticationPrincipal UserPrincipal — Spring Security сам достаёт
// текущего аутентифицированного пользователя из SecurityContext (его туда
// кладёт JwtAuthenticationFilter после проверки токена) и подставляет сюда
// как параметр метода — не нужно вручную лезть в SecurityContextHolder.
@RestController
@RequestMapping("/quiz-attempts")
@Tag(name = "Quiz Attempts", description = "Прохождение квиза: старт попытки, вопросы, ответы")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    @PostMapping
    public ResponseEntity<QuizAttemptDto> start(@Valid @RequestBody QuizAttemptStartDto dto,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        QuizAttemptDto started = quizAttemptService.start(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(started);
    }

    @GetMapping
    public ResponseEntity<List<QuizAttemptDto>> getHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(quizAttemptService.getHistory(principal.getUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAttemptDto> getById(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(quizAttemptService.getById(id, principal.getUser()));
    }

    // Следующий неотвеченный вопрос попытки — вызывается циклически, пока
    // не будут отвечены все totalQuestions вопросов.
    @GetMapping("/{id}/next-question")
    public ResponseEntity<QuestionQuizDto> getNextQuestion(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(quizAttemptService.getNextQuestion(id, principal.getUser()));
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<UserAnswerResultDto> submitAnswer(@PathVariable Long id,
                                                            @Valid @RequestBody UserAnswerSubmitDto dto,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(quizAttemptService.submitAnswer(id, dto, principal.getUser()));
    }

    @PostMapping("/{id}/abandon")
    public ResponseEntity<QuizAttemptDto> abandon(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(quizAttemptService.abandon(id, principal.getUser()));
    }
}
