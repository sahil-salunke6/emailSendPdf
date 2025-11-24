import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.criteria.CriteriaBuilder.In;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class DsFeeDataRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<InterimResponseDTO> criteriaQuery;

    @Mock
    private CriteriaQuery<String> stringCriteriaQuery;

    @Mock
    private CriteriaQuery<LocalDate> dateCriteriaQuery;

    @Mock
    private Root<DrSecurity> securityRoot;

    @Mock
    private Root<DreamDsFee> dsFeeRoot;

    @Mock
    private Join<DrSecurity, DrCountry> countryJoin;

    @Mock
    private Join<DrSecurity, DreamDsFee> dsFeeJoin;

    @Mock
    private Join<DrSecurity, DrTableEntry> tableEntryJoin;

    @Mock
    private TypedQuery<InterimResponseDTO> interimTypedQuery;

    @Mock
    private TypedQuery<String> stringTypedQuery;

    @Mock
    private TypedQuery<LocalDate> dateTypedQuery;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @Mock
    private Subquery<LocalDate> subquery;

    @Mock
    private Expression<LocalDate> dateExpression;

    @InjectMocks
    private DsFeeDataRepository repository;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    }

    @Test
    void testFindSecurityDetails_WithCusip_Success() {
        // Arrange
        String identifier = "12345678";
        String identifierType = "CUSIP";

        setupCommonMocks();
        setupSecurityDetailsMocks();

        InterimResponseDTO expectedDto = createMockInterimResponseDTO();
        List<InterimResponseDTO> resultList = Arrays.asList(expectedDto);

        when(interimTypedQuery.getResultList()).thenReturn(resultList);

        // Act
        InterimResponseDTO result = repository.findSecurityDetails(identifier, identifierType);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(criteriaBuilder).equal(any(Path.class), eq(identifier));
    }

    @Test
    void testFindSecurityDetails_WithSecId_Success() {
        // Arrange
        String identifier = "SEC123";
        String identifierType = "SECID";

        setupCommonMocks();
        setupSecurityDetailsMocks();

        InterimResponseDTO expectedDto = createMockInterimResponseDTO();
        List<InterimResponseDTO> resultList = Arrays.asList(expectedDto);

        when(interimTypedQuery.getResultList()).thenReturn(resultList);

        // Act
        InterimResponseDTO result = repository.findSecurityDetails(identifier, identifierType);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @Test
    void testFindSecurityDetails_WithLowercaseIdentifierType_Success() {
        // Arrange
        String identifier = "12345678";
        String identifierType = "cusip";

        setupCommonMocks();
        setupSecurityDetailsMocks();

        InterimResponseDTO expectedDto = createMockInterimResponseDTO();
        List<InterimResponseDTO> resultList = Arrays.asList(expectedDto);

        when(interimTypedQuery.getResultList()).thenReturn(resultList);

        // Act
        InterimResponseDTO result = repository.findSecurityDetails(identifier, identifierType);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testFindSecurityDetails_InvalidIdentifierType_ThrowsException() {
        // Arrange
        String identifier = "12345678";
        String identifierType = "INVALID";

        setupCommonMocks();
        setupSecurityDetailsMocks();

        // Act & Assert
        ADRApplicationException exception = assertThrows(ADRApplicationException.class,
                () -> repository.findSecurityDetails(identifier, identifierType));

        assertEquals("40003001", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Invalid identifier type"));
    }

    @Test
    void testFindSecurityDetails_NoResultException_ReturnsNull() {
        // Arrange
        String identifier = "12345678";
        String identifierType = "CUSIP";

        setupCommonMocks();
        setupSecurityDetailsMocks();

        when(interimTypedQuery.getResultList()).thenThrow(new NoResultException());

        // Act
        InterimResponseDTO result = repository.findSecurityDetails(identifier, identifierType);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindSecurityDetails_DataAccessException_ThrowsADRException() {
        // Arrange
        String identifier = "12345678";
        String identifierType = "CUSIP";

        setupCommonMocks();
        setupSecurityDetailsMocks();

        when(interimTypedQuery.getResultList()).thenThrow(new TestDataAccessException("DB Error"));

        // Act & Assert
        ADRApplicationException exception = assertThrows(ADRApplicationException.class,
                () -> repository.findSecurityDetails(identifier, identifierType));

        assertEquals("50003002", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Problem occurred while fetching"));
    }

    @Test
    void testFindSecurityDetails_EmptyResultList_ReturnsNull() {
        // Arrange
        String identifier = "12345678";
        String identifierType = "CUSIP";

        setupCommonMocks();
        setupSecurityDetailsMocks();

        when(interimTypedQuery.getResultList()).thenReturn(new ArrayList<>());

        // Act
        InterimResponseDTO result = repository.findSecurityDetails(identifier, identifierType);

        // Assert
        assertNull(result);
    }

    @Test
    void testFindCusipByIssuerName_Success() {
        // Arrange
        String issuerName = "Test Company";
        List<String> expectedCusips = Arrays.asList("CUSIP1", "CUSIP2");

        when(criteriaBuilder.createQuery(String.class)).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.from(DrSecurity.class)).thenReturn(securityRoot);
        when(securityRoot.get(anyString())).thenReturn(path);
        when(stringCriteriaQuery.select(any())).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.where(any(Predicate.class))).thenReturn(stringCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(stringCriteriaQuery)).thenReturn(stringTypedQuery);
        when(stringTypedQuery.getResultList()).thenReturn(expectedCusips);

        // Act
        List<String> result = repository.findCusipByIssuerName(issuerName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedCusips, result);
    }

    @Test
    void testFindCusipByIssuerName_DataAccessException_ThrowsADRException() {
        // Arrange
        String issuerName = "Test Company";

        when(criteriaBuilder.createQuery(String.class)).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.from(DrSecurity.class)).thenReturn(securityRoot);
        when(securityRoot.get(anyString())).thenReturn(path);
        when(stringCriteriaQuery.select(any())).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.where(any(Predicate.class))).thenReturn(stringCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(stringCriteriaQuery)).thenReturn(stringTypedQuery);
        when(stringTypedQuery.getResultList()).thenThrow(new TestDataAccessException("DB Error"));

        // Act & Assert
        ADRApplicationException exception = assertThrows(ADRApplicationException.class,
                () -> repository.findCusipByIssuerName(issuerName));

        assertEquals("50003002", exception.getErrorCode());
    }

    @Test
    void testFindCusipByTickerId_Success() {
        // Arrange
        String tickerId = "TICK123";
        List<String> expectedCusips = Arrays.asList("CUSIP1");

        when(criteriaBuilder.createQuery(String.class)).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.from(DrSecurity.class)).thenReturn(securityRoot);
        when(securityRoot.get(anyString())).thenReturn(path);
        when(stringCriteriaQuery.select(any())).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.where(any(Predicate.class))).thenReturn(stringCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(stringCriteriaQuery)).thenReturn(stringTypedQuery);
        when(stringTypedQuery.getResultList()).thenReturn(expectedCusips);

        // Act
        List<String> result = repository.findCusipByTickerId(tickerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedCusips, result);
    }

    @Test
    void testFindCusipByTickerId_DataAccessException_ThrowsADRException() {
        // Arrange
        String tickerId = "TICK123";

        when(criteriaBuilder.createQuery(String.class)).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.from(DrSecurity.class)).thenReturn(securityRoot);
        when(securityRoot.get(anyString())).thenReturn(path);
        when(stringCriteriaQuery.select(any())).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.where(any(Predicate.class))).thenReturn(stringCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(stringCriteriaQuery)).thenReturn(stringTypedQuery);
        when(stringTypedQuery.getResultList()).thenThrow(new TestDataAccessException("DB Error"));

        // Act & Assert
        ADRApplicationException exception = assertThrows(ADRApplicationException.class,
                () -> repository.findCusipByTickerId(tickerId));

        assertEquals("50003002", exception.getErrorCode());
    }

    @Test
    void testFindCusipBySecId_Success() {
        // Arrange
        String secId = "SEC123";
        String expectedCusip = "CUSIP123";

        when(criteriaBuilder.createQuery(String.class)).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.from(DrSecurity.class)).thenReturn(securityRoot);
        when(securityRoot.get(anyString())).thenReturn(path);
        when(stringCriteriaQuery.select(any())).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.where(any(Predicate.class))).thenReturn(stringCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(stringCriteriaQuery)).thenReturn(stringTypedQuery);
        when(stringTypedQuery.getSingleResult()).thenReturn(expectedCusip);

        // Act
        String result = repository.findCusipBySecId(secId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCusip, result);
    }

    @Test
    void testFindCusipBySecId_DataAccessException_ThrowsADRException() {
        // Arrange
        String secId = "SEC123";

        when(criteriaBuilder.createQuery(String.class)).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.from(DrSecurity.class)).thenReturn(securityRoot);
        when(securityRoot.get(anyString())).thenReturn(path);
        when(stringCriteriaQuery.select(any())).thenReturn(stringCriteriaQuery);
        when(stringCriteriaQuery.where(any(Predicate.class))).thenReturn(stringCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(stringCriteriaQuery)).thenReturn(stringTypedQuery);
        when(stringTypedQuery.getSingleResult()).thenThrow(new TestDataAccessException("DB Error"));

        // Act & Assert
        ADRApplicationException exception = assertThrows(ADRApplicationException.class,
                () -> repository.findCusipBySecId(secId));

        assertEquals("50003002", exception.getErrorCode());
    }

    @Test
    void testFindLatestRecordDateBySecId_Success() {
        // Arrange
        String secId = "SEC123";
        LocalDate expectedDate = LocalDate.of(2024, 1, 15);

        when(criteriaBuilder.createQuery(LocalDate.class)).thenReturn(dateCriteriaQuery);
        when(dateCriteriaQuery.from(DreamDsFee.class)).thenReturn(dsFeeRoot);
        when(dsFeeRoot.get(anyString())).thenReturn(path);
        when(path.as(LocalDate.class)).thenReturn(dateExpression);
        when(criteriaBuilder.greatest(any())).thenReturn(dateExpression);
        when(dateCriteriaQuery.select(any())).thenReturn(dateCriteriaQuery);
        when(dateCriteriaQuery.where(any(Predicate.class))).thenReturn(dateCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(dateCriteriaQuery)).thenReturn(dateTypedQuery);
        when(dateTypedQuery.getSingleResult()).thenReturn(expectedDate);

        // Act
        LocalDate result = repository.findLatestRecordDateBySecId(secId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDate, result);
    }

    @Test
    void testFindLatestRecordDateBySecId_DataAccessException_ThrowsADRException() {
        // Arrange
        String secId = "SEC123";

        when(criteriaBuilder.createQuery(LocalDate.class)).thenReturn(dateCriteriaQuery);
        when(dateCriteriaQuery.from(DreamDsFee.class)).thenReturn(dsFeeRoot);
        when(dsFeeRoot.get(anyString())).thenReturn(path);
        when(path.as(LocalDate.class)).thenReturn(dateExpression);
        when(criteriaBuilder.greatest(any())).thenReturn(dateExpression);
        when(dateCriteriaQuery.select(any())).thenReturn(dateCriteriaQuery);
        when(dateCriteriaQuery.where(any(Predicate.class))).thenReturn(dateCriteriaQuery);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(entityManager.createQuery(dateCriteriaQuery)).thenReturn(dateTypedQuery);
        when(dateTypedQuery.getSingleResult()).thenThrow(new TestDataAccessException("DB Error"));

        // Act & Assert
        ADRApplicationException exception = assertThrows(ADRApplicationException.class,
                () -> repository.findLatestRecordDateBySecId(secId));

        assertEquals("50003002", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("latest record date"));
    }

    // Helper methods
    @SuppressWarnings("unchecked")
    private void setupCommonMocks() {
        when(criteriaBuilder.createQuery(InterimResponseDTO.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(DrSecurity.class)).thenReturn(securityRoot);

        // Create a mock for drTableEntry join
        Join<DrSecurity, DrTableEntry> mockTableEntryJoin = mock(Join.class);

        // Mock multiple join calls with different return values
        when(securityRoot.join(eq("country"), any(JoinType.class))).thenReturn((Join) countryJoin);
        when(securityRoot.join(eq("dreamDsFees"), any(JoinType.class))).thenReturn((Join) dsFeeJoin);
        when(securityRoot.join(eq("drTableEntry"), any(JoinType.class))).thenReturn((Join) mockTableEntryJoin);

        when(criteriaBuilder.treat(any(Join.class), eq(DrTableEntry.class))).thenReturn(tableEntryJoin);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(interimTypedQuery);

        // Mock the on() method for joins
        when(countryJoin.on(any(Predicate.class))).thenReturn(countryJoin);
        when(dsFeeJoin.on(any(Predicate.class))).thenReturn(dsFeeJoin);
    }

    @SuppressWarnings("unchecked")
    private void setupSecurityDetailsMocks() {
        // Mock Path objects for different entity attributes
        Path<Object> countryIdPath = mock(Path.class);
        Path<Object> securityIdPath = mock(Path.class);
        Path<Object> cusipPath = mock(Path.class);
        Path<Object> programCodePath = mock(Path.class);
        Path<Object> recordDatePath = mock(Path.class);

        // Mock get() calls for different paths
        when(securityRoot.get("countryId")).thenReturn(countryIdPath);
        when(securityRoot.get("securityId")).thenReturn(securityIdPath);
        when(securityRoot.get("cusipNb")).thenReturn(cusipPath);
        when(securityRoot.get("programCode")).thenReturn(programCodePath);
        when(securityRoot.get(anyString())).thenReturn(path);

        when(countryJoin.get("countryId")).thenReturn(countryIdPath);
        when(countryJoin.get(anyString())).thenReturn(path);

        when(dsFeeJoin.get("securityIdentifier")).thenReturn(securityIdPath);
        when(dsFeeJoin.get("recordDate")).thenReturn(recordDatePath);
        when(dsFeeJoin.get(anyString())).thenReturn(path);

        when(tableEntryJoin.get(anyString())).thenReturn(path);

        // Mock Path.in() for program code
        when(programCodePath.in(anyString(), anyString(), anyString())).thenReturn(predicate);
        when(path.in(anyString(), anyString(), anyString())).thenReturn(predicate);

        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(criteriaBuilder.isNull(any())).thenReturn(predicate);

        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
        when(criteriaQuery.select(any(Selection.class))).thenReturn(criteriaQuery);

        // Mock subquery for latest record date
        when(criteriaQuery.subquery(LocalDate.class)).thenReturn(subquery);
        when(subquery.from(DreamDsFee.class)).thenReturn(dsFeeRoot);

        Path<Object> dsFeeSecIdPath = mock(Path.class);
        Path<Object> dsFeeRecordDatePath = mock(Path.class);

        when(dsFeeRoot.get("securityIdentifier")).thenReturn(dsFeeSecIdPath);
        when(dsFeeRoot.get("recordDate")).thenReturn(dsFeeRecordDatePath);
        when(dsFeeRecordDatePath.as(LocalDate.class)).thenReturn(dateExpression);
        when(recordDatePath.as(LocalDate.class)).thenReturn(dateExpression);

        when(criteriaBuilder.greatest(any(Expression.class))).thenReturn(dateExpression);
        when(subquery.select(any(Expression.class))).thenReturn(subquery);
        when(subquery.where(any(Predicate.class))).thenReturn(subquery);

        // Mock construct - return a CompoundSelection
        CompoundSelection<InterimResponseDTO> compoundSelection = mock(CompoundSelection.class);
        when(criteriaBuilder.construct(eq(InterimResponseDTO.class),
                any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(compoundSelection);
    }

    private InterimResponseDTO createMockInterimResponseDTO() {
        // Create and return a mock DTO with test data
        // Note: Adjust constructor parameters based on actual InterimResponseDTO constructor
        try {
            return new InterimResponseDTO(
                    "Test Issuer",           // issuerName
                    "USA",                    // countryName
                    "SPONSOR1",               // sponsorCode
                    "100",                    // osFi
                    "50",                     // adrFi
                    "ISS001",                 // issuerNameNumber
                    "SEC123",                 // securityId
                    "CUSIP123",               // cusipNb
                    "TICK",                   // drTickerId
                    "A",                      // statusFlag
                    "A",                      // programCode
                    "Active",                 // tableResultText
                    "100.00",                 // marketPriceUsd
                    LocalDate.of(2024, 1, 15) // recordDate
            );
        } catch (Exception e) {
            // If constructor doesn't match, create using reflection or builder
            // Fallback: return a mock object
            InterimResponseDTO mockDto = mock(InterimResponseDTO.class);
            when(mockDto.toString()).thenReturn("Mock InterimResponseDTO");
            return mockDto;
        }
    }

    // Custom DataAccessException for testing
    private static class TestDataAccessException extends DataAccessException {
        public TestDataAccessException(String msg) {
            super(msg);
        }
    }
}