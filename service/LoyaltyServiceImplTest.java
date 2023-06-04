package com.mscomm.loyaltyservice.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mscomm.loyaltyservice.dto.ResponseDto;
import com.mscomm.loyaltyservice.dto.UserDto;
import com.mscomm.loyaltyservice.entity.Loyalty;
import com.mscomm.loyaltyservice.repository.LoyaltyRepository;
import com.mscomm.loyaltyservice.service.impl.LoyaltyServiceImpl;

class LoyaltyServiceImplTest {
    private RestTemplate restTemplate;
    private LoyaltyRepository loyaltyRepository;
    private LoyaltyService loyaltyService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        loyaltyRepository = mock(LoyaltyRepository.class);
        loyaltyService = new LoyaltyServiceImpl(restTemplate, loyaltyRepository);
    }

//    @Test
//    void testGetUsersAndAddLoyalty_ExistingUser_ReturnsExistingLoyalty() {
//        // Arrange
//        Long userId = 1L;
//        ResponseDto responseDto = new ResponseDto();
//        UserDto userDto = new UserDto();
//        userDto.setId(userId);
//        userDto.setName("John Doe");
//        userDto.setEmail("john.doe@example.com");
//        responseDto.setUser(userDto);
//
//        when(restTemplate.getForEntity(anyString(), eq(ResponseDto.class)))
//                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));
//
//        Loyalty existingLoyalty = new Loyalty();
//        existingLoyalty.setId(1L);
//        when(loyaltyRepository.findByUserNameAndUserEmail(eq(userDto.getName()), eq(userDto.getEmail())))
//                .thenReturn(existingLoyalty);
//
//        // Act
//        Loyalty result = loyaltyService.getUsersAndAddLoyalty(userId);
//
//        // Assert
//        verify(restTemplate).getForEntity(anyString(), eq(ResponseDto.class));
//        verify(loyaltyRepository).findByUserNameAndUserEmail(eq(userDto.getName()), eq(userDto.getEmail()));
//        assertNull(result.getUserId());
//        assertEquals(existingLoyalty, result);
//    }

    @Test
    void testGetUsersAndAddLoyalty_NonExistingUser_ReturnsNewLoyalty() {
        // Arrange
        Long userId = 1L;
        ResponseDto responseDto = new ResponseDto();
        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");
        responseDto.setUser(userDto);

        when(restTemplate.getForEntity(anyString(), eq(ResponseDto.class)))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));

        when(loyaltyRepository.findByUserNameAndUserEmail(eq(userDto.getName()), eq(userDto.getEmail())))
                .thenReturn(null);

        when(loyaltyRepository.save(any(Loyalty.class))).thenAnswer(invocation -> {
            Loyalty savedLoyalty = invocation.getArgument(0);
            savedLoyalty.setId(1L); // Set a sample ID for the saved loyalty
            return savedLoyalty;
        });

        // Act
        Loyalty result = loyaltyService.getUsersAndAddLoyalty(userId);

        // Assert
        verify(restTemplate).getForEntity(anyString(), eq(ResponseDto.class));
        verify(loyaltyRepository).findByUserNameAndUserEmail(eq(userDto.getName()), eq(userDto.getEmail()));
        verify(loyaltyRepository).save(any(Loyalty.class));
        assertEquals(userDto.getId(), result.getUserId());
        assertEquals(userDto.getName(), result.getUserName());
        assertEquals(userDto.getEmail(), result.getUserEmail());
        assertEquals("0", result.getCoins());
        assertEquals("0", result.getDiscountedvalue());
        assertEquals(1L, result.getId()); // ID is set by the service
    }

    @Test
    void testGetUserCoins_ReturnsCoins() {
        // Arrange
        Long userId = 1L;
        Loyalty loyalty = new Loyalty();
        loyalty.setCoins("100");
        when(loyaltyRepository.findByUserId(userId)).thenReturn(loyalty);

        // Act
        String result = loyaltyService.getUserCoins(userId);

        // Assert
        verify(loyaltyRepository).findByUserId(userId);
        assertEquals(loyalty.getCoins(), result);
    }

    @Test
    void testUpdateCoins_ExistingLoyalty_UpdatesCoins() {
        // Arrange
        Long userId = 1L;
        Loyalty loyalty = new Loyalty();
        loyalty.setCoins("5");
        when(loyaltyRepository.findByUserId(userId)).thenReturn(loyalty);

        // Act
        loyaltyService.updateCoins(userId);

        // Assert
        verify(loyaltyRepository).findByUserId(userId);
        verify(loyaltyRepository).save(loyalty);
        assertEquals("6", loyalty.getCoins());
        assertEquals("30", loyalty.getDiscountedvalue());
    }

    @Test
    void testUpdateCoins_NonExistingLoyalty_DoesNotUpdate() {
        // Arrange
        Long userId = 1L;
        when(loyaltyRepository.findByUserId(userId)).thenReturn(null);

        // Act
        loyaltyService.updateCoins(userId);

        // Assert
        verify(loyaltyRepository).findByUserId(userId);
        verify(loyaltyRepository, never()).save(any(Loyalty.class));
    }

    @Test
    void testReduceCoins_ExistingLoyalty_UpdatesCoins() {
        // Arrange
        Long userId = 1L;
        Loyalty loyalty = new Loyalty();
        loyalty.setCoins("5");
        when(loyaltyRepository.findByUserId(userId)).thenReturn(loyalty);

        // Act
        loyaltyService.reduceCoins(userId);

        // Assert
        verify(loyaltyRepository).findByUserId(userId);
        verify(loyaltyRepository).save(loyalty);
        assertEquals("3", loyalty.getCoins());
        assertEquals("15", loyalty.getDiscountedvalue());
    }

    @Test
    void testReduceCoins_ExistingLoyalty_CoinsCannotBeNegative() {
        // Arrange
        Long userId = 1L;
        Loyalty loyalty = new Loyalty();
        loyalty.setCoins("1");
        when(loyaltyRepository.findByUserId(userId)).thenReturn(loyalty);

        // Act
        loyaltyService.reduceCoins(userId);

        // Assert
        verify(loyaltyRepository).findByUserId(userId);
        verify(loyaltyRepository).save(loyalty);
        assertEquals("0", loyalty.getCoins());
        assertEquals("0", loyalty.getDiscountedvalue());
    }

    @Test
    void testReduceCoins_NonExistingLoyalty_DoesNotUpdate() {
        // Arrange
        Long userId = 1L;
        when(loyaltyRepository.findByUserId(userId)).thenReturn(null);

        // Act
        loyaltyService.reduceCoins(userId);

        // Assert
        verify(loyaltyRepository).findByUserId(userId);
        verify(loyaltyRepository, never()).save(any(Loyalty.class));
    }
}
