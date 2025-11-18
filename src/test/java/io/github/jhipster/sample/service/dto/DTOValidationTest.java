package io.github.jhipster.sample.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jhipster.sample.IntegrationTest;
import io.github.jhipster.sample.domain.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for DTO validation.
 */
@IntegrationTest
class DTOValidationTest {

    @Autowired
    private Validator validator;

    private AdminUserDTO adminUserDTO;
    private UserDTO userDTO;
    private PasswordChangeDTO passwordChangeDTO;

    @BeforeEach
    void setUp() {
        adminUserDTO = new AdminUserDTO();
        adminUserDTO.setLogin("testuser");
        adminUserDTO.setEmail("test@example.com");
        adminUserDTO.setFirstName("Test");
        adminUserDTO.setLastName("User");
        adminUserDTO.setActivated(true);
        adminUserDTO.setLangKey("en");

        userDTO = new UserDTO();
        userDTO.setLogin("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setActivated(true);
        userDTO.setLangKey("en");

        passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("currentPassword");
        passwordChangeDTO.setNewPassword("newPassword");
    }

    @Test
    void testValidAdminUserDTO() {
        Set<ConstraintViolation<AdminUserDTO>> violations = validator.validate(adminUserDTO);
        assertThat(violations).isEmpty();
    }

    @Test
    void testAdminUserDTOWithInvalidEmail() {
        adminUserDTO.setEmail("invalid-email");
        Set<ConstraintViolation<AdminUserDTO>> violations = validator.validate(adminUserDTO);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testAdminUserDTOWithNullEmail() {
        adminUserDTO.setEmail(null);
        Set<ConstraintViolation<AdminUserDTO>> violations = validator.validate(adminUserDTO);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testAdminUserDTOWithNullLogin() {
        adminUserDTO.setLogin(null);
        Set<ConstraintViolation<AdminUserDTO>> violations = validator.validate(adminUserDTO);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testAdminUserDTOFromUser() {
        User user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActivated(true);
        user.setLangKey("en");

        AdminUserDTO dto = new AdminUserDTO(user);
        assertThat(dto.getLogin()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getFirstName()).isEqualTo("Test");
        assertThat(dto.getLastName()).isEqualTo("User");
        assertThat(dto.isActivated()).isTrue();
        assertThat(dto.getLangKey()).isEqualTo("en");
    }

    @Test
    void testValidUserDTO() {
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        assertThat(violations).isEmpty();
    }

    @Test
    void testUserDTOWithInvalidEmail() {
        userDTO.setEmail("invalid-email");
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testUserDTOFromUser() {
        User user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActivated(true);
        user.setLangKey("en");

        UserDTO dto = new UserDTO(user);
        assertThat(dto.getLogin()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getFirstName()).isEqualTo("Test");
        assertThat(dto.getLastName()).isEqualTo("User");
        assertThat(dto.isActivated()).isTrue();
        assertThat(dto.getLangKey()).isEqualTo("en");
    }

    @Test
    void testValidPasswordChangeDTO() {
        Set<ConstraintViolation<PasswordChangeDTO>> violations = validator.validate(passwordChangeDTO);
        assertThat(violations).isEmpty();
    }

    @Test
    void testPasswordChangeDTOWithNullCurrentPassword() {
        passwordChangeDTO.setCurrentPassword(null);
        Set<ConstraintViolation<PasswordChangeDTO>> violations = validator.validate(passwordChangeDTO);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testPasswordChangeDTOWithNullNewPassword() {
        passwordChangeDTO.setNewPassword(null);
        Set<ConstraintViolation<PasswordChangeDTO>> violations = validator.validate(passwordChangeDTO);
        assertThat(violations).isNotEmpty();
    }
}
