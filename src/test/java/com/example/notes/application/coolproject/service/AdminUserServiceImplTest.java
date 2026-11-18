package com.example.notes.application.coolproject.service;

import static com.example.notes.application.coolproject.TestData.ADMIN_EMAIL;
import static com.example.notes.application.coolproject.TestData.ADMIN_ID;
import static com.example.notes.application.coolproject.TestData.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.OperationNotAllowedException;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.UserMapper;
import com.example.notes.application.coolproject.repository.ExpenseRepository;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.service.admin.AdminUserServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private ExpenseRepository expenseRepository;
  @Mock private UserMapper userMapper;

  @InjectMocks private AdminUserServiceImpl adminUserService;

  private final UserEntity user = TestData.regularUser();
  private final UserEntity admin = TestData.user(ADMIN_ID, ADMIN_EMAIL, UserRole.ADMIN);

  @Test
  void listsUsersAsPage() {
    var pageable = PageRequest.of(0, 20);
    var response = TestData.userResponse(USER_ID, user.getEmail(), UserRole.USER);
    when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 1));
    when(userMapper.toResponse(user)).thenReturn(response);

    var page = adminUserService.findAll(pageable);

    assertThat(page.content()).containsExactly(response);
    assertThat(page.totalElements()).isEqualTo(1);
    assertThat(page.size()).isEqualTo(20);
  }

  @Test
  void missingUserThrowsUserNotFound() {
    when(userRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminUserService.findById(9L))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void promotesAnotherUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    adminUserService.changeRole(ADMIN_EMAIL, USER_ID, UserRole.ADMIN);

    assertThat(user.getUserRole()).isEqualTo(UserRole.ADMIN);
  }

  @Test
  void cannotDemoteThemselves() {
    when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

    assertThatThrownBy(() -> adminUserService.changeRole(ADMIN_EMAIL, ADMIN_ID, UserRole.USER))
        .isInstanceOf(OperationNotAllowedException.class);
    assertThat(admin.getUserRole()).isEqualTo(UserRole.ADMIN);
  }

  @Test
  void blocksAnotherUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    adminUserService.changeStatus(ADMIN_EMAIL, USER_ID, false);

    assertThat(user.isEnabled()).isFalse();
  }

  @Test
  void cannotBlockThemselves() {
    when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

    assertThatThrownBy(() -> adminUserService.changeStatus(ADMIN_EMAIL, ADMIN_ID, false))
        .isInstanceOf(OperationNotAllowedException.class);
    assertThat(admin.isEnabled()).isTrue();
  }

  @Test
  void deletesExpensesBeforeTheUser() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    adminUserService.delete(ADMIN_EMAIL, USER_ID);

    var order = inOrder(expenseRepository, userRepository);
    order.verify(expenseRepository).deleteAllByUserId(USER_ID);
    order.verify(userRepository).delete(user);
  }

  @Test
  void cannotDeleteThemselves() {
    when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));

    assertThatThrownBy(() -> adminUserService.delete(ADMIN_EMAIL, ADMIN_ID))
        .isInstanceOf(OperationNotAllowedException.class);
    verify(expenseRepository, never()).deleteAllByUserId(any());
    verify(userRepository, never()).delete(any());
  }

  @Test
  void deletingMissingUserThrowsUserNotFound() {
    when(userRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminUserService.delete(ADMIN_EMAIL, 9L))
        .isInstanceOf(UserNotFoundException.class);
  }
}
