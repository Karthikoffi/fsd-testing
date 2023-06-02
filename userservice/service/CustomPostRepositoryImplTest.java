//package com.mscomm.userservice.repository;
package com.mscomm.userservice.service;
import com.mscomm.userservice.entity.User;
import com.mscomm.userservice.repository.CustomPostRepository;
import com.mscomm.userservice.service.impl.CustomPostRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CustomPostRepositoryImplTest {

    private CustomPostRepository customPostRepository;
    private EntityManager entityManager;
    private TypedQuery typedQuery;

    @BeforeEach
    public void setup() {
        entityManager = mock(EntityManager.class);
        typedQuery = mock(TypedQuery.class);
        customPostRepository = new CustomPostRepositoryImpl();
    }

    @Test
    public void findByIdAndLock_ValidIdAndLockMode_ReturnsUser() {
        // Arrange
        Long UserId = 1L;
        LockModeType lockMode = LockModeType.PESSIMISTIC_WRITE;
        User expectedUser = new User();

        when(entityManager.find(User.class, UserId, lockMode)).thenReturn(expectedUser);
        
        // Act
        User result = customPostRepository.findByIdAndLock(UserId, lockMode);

        // Assert
        assertEquals(expectedUser, result);
        verify(entityManager, times(1)).find(User.class, UserId, lockMode);
    }
}
