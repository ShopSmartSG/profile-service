package sg.edu.nus.iss.profile_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.iss.profile_service.model.Merchant;
import sg.edu.nus.iss.profile_service.repository.MerchantRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantService merchantService;

    private Merchant merchant;
    private UUID merchantId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        merchantId = UUID.randomUUID();
        merchant = new Merchant();
        merchant.setMerchantId(merchantId);
        merchant.setMerchantName("Test Merchant");
        merchant.setMerchantEmail("test@example.com");
    }

    @Test
    void testGetMerchant() {
        when(merchantRepository.findByMerchantIdAndDeletedFalse(merchantId)).thenReturn(Optional.of(merchant));
        Optional<Merchant> result = merchantService.getMerchant(merchantId);
        assertTrue(result.isPresent());
        assertEquals(merchantId, result.get().getMerchantId());
    }

    @Test
    void testUpdateMerchant() {
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);
        Boolean result = merchantService.updateMerchant(merchant);
        assertTrue(result);
        verify(merchantRepository, times(1)).save(merchant);
    }

    @Test
    void testGetMerchantByEmailId() {
        when(merchantRepository.findByMerchantEmailAndDeletedFalse("test@example.com")).thenReturn(Optional.of(merchant));
        Optional<Merchant> result = merchantService.getMerchantByEmailId("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getMerchantEmail());
    }

    @Test
    void testGetAllMerchants() {
        when(merchantRepository.findAllByDeletedFalse()).thenReturn(List.of(merchant));
        List<Merchant> result = merchantService.getAllMerchants();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testRegisterMerchant() {
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);
        Merchant result = merchantService.registerMerchant(merchant);
        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
    }

    @Test
    void testBlacklistMerchant() {
        merchant.setBlacklisted(true);
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);
        Boolean result = merchantService.updateMerchant(merchant);
        assertTrue(result);
        assertTrue(merchant.isBlacklisted());
        verify(merchantRepository, times(1)).save(merchant);
    }

    @Test
    void testUnblacklistMerchant() {
        merchant.setBlacklisted(false);
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);
        Boolean result = merchantService.updateMerchant(merchant);
        assertTrue(result);
        assertFalse(merchant.isBlacklisted());
        verify(merchantRepository, times(1)).save(merchant);
    }


}