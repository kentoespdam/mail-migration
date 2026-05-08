# Graph Report - mail-service  (2026-05-08)

## Corpus Check
- 321 files · ~472,086 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1540 nodes · 2403 edges · 76 communities detected
- Extraction: 63% EXTRACTED · 37% INFERRED · 0% AMBIGUOUS · INFERRED: 889 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]
- [[_COMMUNITY_Community 73|Community 73]]
- [[_COMMUNITY_Community 74|Community 74]]
- [[_COMMUNITY_Community 75|Community 75]]
- [[_COMMUNITY_Community 77|Community 77]]
- [[_COMMUNITY_Community 79|Community 79]]
- [[_COMMUNITY_Community 80|Community 80]]
- [[_COMMUNITY_Community 81|Community 81]]
- [[_COMMUNITY_Community 86|Community 86]]
- [[_COMMUNITY_Community 87|Community 87]]

## God Nodes (most connected - your core abstractions)
1. `findById()` - 58 edges
2. `from()` - 43 edges
3. `userIdLong()` - 31 edges
4. `getDbValue()` - 22 edges
5. `resolve()` - 20 edges
6. `MailControllerTest` - 19 edges
7. `MailController` - 16 edges
8. `MailFolderControllerTest` - 15 edges
9. `PublicationFileStorageServiceTest` - 15 edges
10. `getUsername()` - 13 edges

## Surprising Connections (you probably didn't know these)
- `from()` --calls--> `getRoles()`  [INFERRED]
  src/main/java/id/perumdamts/mail/security/MailPrincipal.java → src/main/java/id/perumdamts/mail/security/AppWriteUser.java
- `getRoles()` --calls--> `from()`  [INFERRED]
  src/main/java/id/perumdamts/mail/security/AppWriteUser.java → src/main/java/id/perumdamts/mail/security/CachedUserInfo.java
- `fromCachedInfo()` --calls--> `toMailPrincipal()`  [INFERRED]
  src/main/java/id/perumdamts/mail/security/MailPrincipal.java → src/main/java/id/perumdamts/mail/security/CachedUserInfo.java
- `isAdmin()` --calls--> `getValue()`  [INFERRED]
  src/main/java/id/perumdamts/mail/security/AppWriteUser.java → src/main/java/id/perumdamts/mail/enums/PublicationStatus.java
- `isSystem()` --calls--> `getValue()`  [INFERRED]
  src/main/java/id/perumdamts/mail/security/AppWriteUser.java → src/main/java/id/perumdamts/mail/enums/PublicationStatus.java

## Communities

### Community 0 - "Community 0"
Cohesion: 0.02
Nodes (26): MailArchiveNotifCommandService, MailArchiveQueryService, MailSignatureControllerRateLimitTest, MailSignatureControllerTest, DocumentTypeCommandService, DocumentTypeCommandServiceTest, findById(), HrServiceClientFallback (+18 more)

### Community 1 - "Community 1"
Cohesion: 0.03
Nodes (13): MailAttachmentController, MailController, MailControllerTest, MailFolderController, MailRecipientController, PublicationController, AllowedFileTypeController, DocumentTypeController (+5 more)

### Community 2 - "Community 2"
Cohesion: 0.03
Nodes (17): PagedRequest, CacheSerializationTest, ArchiveQueryRepository, AttachmentQueryRepository, DocumentTypeQueryRepository, MailArchiveNotifQueryRepository, MailCategoryQueryRepository, MailQueryRepository (+9 more)

### Community 3 - "Community 3"
Cohesion: 0.04
Nodes (13): MailFolderControllerTest, isMovable(), isPersonalFolder(), MailFolderCommandService, MailFolderQueryService, FolderCounterRepository, MailFolderRepository, AuditTrailService (+5 more)

### Community 4 - "Community 4"
Cohesion: 0.03
Nodes (15): AttachmentCommandService, AttachmentCommandServiceTest, AttachmentMapper, AttachmentMapperImpl, AttachmentQueryService, Attachment, Mail, MailArchive (+7 more)

### Community 5 - "Community 5"
Cohesion: 0.03
Nodes (17): MailFolder, MailFolderTest, Publication, PublicationTest, AllowedFileType, DocumentType, DocumentTypeTest, MailCategory (+9 more)

### Community 6 - "Community 6"
Cohesion: 0.05
Nodes (13): MailRecipientControllerTest, ArchivePublishedEventListener, ArchivePublishedEventListenerTest, jabatanId(), jabatanNama(), getData(), HrServiceClient, HrServiceClient (+5 more)

### Community 7 - "Community 7"
Cohesion: 0.05
Nodes (9): AttachmentFileStorageService, AttachmentFileStorageServiceTest, PageRequest, resolve(), PublicationQueryRepositoryTest, PublicationFileStorageService, PublicationFileStorageServiceTest, CacheKeyUtil (+1 more)

### Community 8 - "Community 8"
Cohesion: 0.03
Nodes (23): SqidMapper, CacheConfig, CacheNames, CacheTtl, PageImplMixin, PageJacksonModule, PageRequestMixin, DocumentTypeMapperImpl (+15 more)

### Community 9 - "Community 9"
Cohesion: 0.04
Nodes (16): ArchiveMapperImpl, ArchiveMapper, HasSqid, PrintLog, PrintLogTest, PublicationControllerTest, UserTask, MailResponseTimeListener (+8 more)

### Community 10 - "Community 10"
Cohesion: 0.05
Nodes (12): ArchiveReportRequest, ArchiveSearchRequest, DocumentTypeParams, MailFolderMailsParams, MailLookupParams, MailReportRequest, MailSearchRequest, MailCategoryParams (+4 more)

### Community 11 - "Community 11"
Cohesion: 0.05
Nodes (7): AbstractArchiveNumberGenerator, MailArchiveCommandService, MailArchiveController, MailArchiveAccessRepository, MailArchiveNotifLogRepository, LongRomanArchiveNumberGenerator, ShortArchiveNumberGenerator

### Community 12 - "Community 12"
Cohesion: 0.09
Nodes (14): AllowedFileTypeDto, DocumentTypeLookup, DocumentTypeResponse, MailFolderLookup, HasSqid, MailResponse, MailSummaryResponse, MailCategoryLookup (+6 more)

### Community 13 - "Community 13"
Cohesion: 0.1
Nodes (7): AllowedFileTypeService, JpaPageRequest, JpaSearchRequest, AllowedFileTypeRepository, JpaPageRequest, PublicationMapperImpl, PublicationMapper

### Community 14 - "Community 14"
Cohesion: 0.11
Nodes (5): MailRecipient, MailQueryCacheListener, RecipientReadStatusListener, RecipientReadStatusListenerTest, RecipientMapper

### Community 15 - "Community 15"
Cohesion: 0.08
Nodes (5): AbstractMailNumberGenerator, BmsMailNumberGenerator, BpnMailNumberGenerator, DefaultMailNumberGenerator, SmdMailNumberGenerator

### Community 16 - "Community 16"
Cohesion: 0.11
Nodes (5): AttachmentMapper, AttachmentController, MailMapper, MailMapperImpl, MailMapper

### Community 17 - "Community 17"
Cohesion: 0.13
Nodes (4): MailThreadNode, MailThreadService, MailThreadServiceTest, MailTrackService

### Community 18 - "Community 18"
Cohesion: 0.13
Nodes (10): getValue(), OncePerRequestFilter, AppWriteAuthFilter, getRoles(), isAdmin(), isSystem(), toMailPrincipal(), from() (+2 more)

### Community 19 - "Community 19"
Cohesion: 0.17
Nodes (3): MailNumberGenerator, AbstractMailNumberGenerator, MailNumberGeneratorDelegator

### Community 20 - "Community 20"
Cohesion: 0.17
Nodes (3): ArchiveNumberGenerator, AbstractArchiveNumberGenerator, ArchiveNumberGeneratorDelegator

### Community 21 - "Community 21"
Cohesion: 0.17
Nodes (3): MessageTemplateService, MessageTemplateMapperImpl, MessageTemplateMapper

### Community 22 - "Community 22"
Cohesion: 0.2
Nodes (6): ErrorDecoder, HrServiceConfig, HrServiceErrorDecoder, HrServiceException, RuntimeException, UnauthorizedException

### Community 23 - "Community 23"
Cohesion: 0.22
Nodes (1): GlobalExceptionHandler

### Community 24 - "Community 24"
Cohesion: 0.22
Nodes (1): MessageTemplateController

### Community 25 - "Community 25"
Cohesion: 0.25
Nodes (2): MailSignatureController, RateLimitService

### Community 26 - "Community 26"
Cohesion: 0.32
Nodes (2): ArchiveNumberParserUtil, ArchiveNumberParserUtilTest

### Community 27 - "Community 27"
Cohesion: 0.33
Nodes (1): PublicationCommandHandler

### Community 28 - "Community 28"
Cohesion: 0.4
Nodes (1): MailTypeRepository

### Community 29 - "Community 29"
Cohesion: 0.4
Nodes (1): MessageTemplateMapper

### Community 30 - "Community 30"
Cohesion: 0.4
Nodes (1): AllowedFileTypeParams

### Community 31 - "Community 31"
Cohesion: 0.4
Nodes (1): OpenApiConfig

### Community 32 - "Community 32"
Cohesion: 0.5
Nodes (1): JooqConfig

### Community 33 - "Community 33"
Cohesion: 0.4
Nodes (1): MailArchiveNotifQueryService

### Community 34 - "Community 34"
Cohesion: 0.4
Nodes (1): PublicationQueryHandler

### Community 35 - "Community 35"
Cohesion: 0.5
Nodes (1): BooleanYesNoConverter

### Community 36 - "Community 36"
Cohesion: 0.5
Nodes (1): MailResponseTime

### Community 37 - "Community 37"
Cohesion: 0.5
Nodes (1): MailOrgStatisticQueryRepository

### Community 38 - "Community 38"
Cohesion: 0.5
Nodes (1): MailOrgStatisticRepository

### Community 39 - "Community 39"
Cohesion: 0.5
Nodes (1): MailTypeMapper

### Community 40 - "Community 40"
Cohesion: 0.5
Nodes (1): DocumentTypeMapper

### Community 41 - "Community 41"
Cohesion: 0.5
Nodes (1): ArchiveMapper

### Community 42 - "Community 42"
Cohesion: 0.67
Nodes (1): MailNotificationListener

### Community 43 - "Community 43"
Cohesion: 0.5
Nodes (1): MailSentEventListener

### Community 44 - "Community 44"
Cohesion: 0.67
Nodes (2): AsyncConfigurer, AsyncConfig

### Community 45 - "Community 45"
Cohesion: 0.5
Nodes (1): MailNumberGenerator

### Community 50 - "Community 50"
Cohesion: 0.67
Nodes (1): MailServiceApplication

### Community 51 - "Community 51"
Cohesion: 0.67
Nodes (1): SqidEntity

### Community 52 - "Community 52"
Cohesion: 0.67
Nodes (1): AttachmentDownloadHistory

### Community 53 - "Community 53"
Cohesion: 0.67
Nodes (1): MailArchiveAccess

### Community 54 - "Community 54"
Cohesion: 0.67
Nodes (1): MailActionLog

### Community 56 - "Community 56"
Cohesion: 0.67
Nodes (1): MailResponseTimeQueryRepository

### Community 57 - "Community 57"
Cohesion: 0.67
Nodes (1): MailArchiveNotifRepository

### Community 58 - "Community 58"
Cohesion: 0.67
Nodes (1): MailArchiveRepository

### Community 59 - "Community 59"
Cohesion: 0.67
Nodes (1): MailActionLogRepository

### Community 60 - "Community 60"
Cohesion: 0.67
Nodes (1): MailCategoryStatisticRepository

### Community 61 - "Community 61"
Cohesion: 0.67
Nodes (1): QuickMessageMapper

### Community 62 - "Community 62"
Cohesion: 0.67
Nodes (1): MailFolderMapper

### Community 63 - "Community 63"
Cohesion: 0.67
Nodes (1): PublicationMapper

### Community 64 - "Community 64"
Cohesion: 0.67
Nodes (1): PublicationNotificationListener

### Community 65 - "Community 65"
Cohesion: 0.67
Nodes (1): MailStatisticListener

### Community 66 - "Community 66"
Cohesion: 0.67
Nodes (1): JacksonConfig

### Community 67 - "Community 67"
Cohesion: 0.67
Nodes (1): SecurityConfig

### Community 68 - "Community 68"
Cohesion: 0.67
Nodes (1): AppWritePropertiesConfig

### Community 69 - "Community 69"
Cohesion: 0.67
Nodes (1): WebClientConfig

### Community 71 - "Community 71"
Cohesion: 1.0
Nodes (1): MailArchiveNotifLog

### Community 72 - "Community 72"
Cohesion: 1.0
Nodes (1): MailArchiveNotif

### Community 73 - "Community 73"
Cohesion: 1.0
Nodes (1): ArchiveLocation

### Community 74 - "Community 74"
Cohesion: 1.0
Nodes (1): MessageTemplateRepository

### Community 75 - "Community 75"
Cohesion: 1.0
Nodes (1): AttachmentDownloadHistoryRepository

### Community 77 - "Community 77"
Cohesion: 1.0
Nodes (1): MailComponentDto

### Community 79 - "Community 79"
Cohesion: 1.0
Nodes (1): RecipientComponentDto

### Community 80 - "Community 80"
Cohesion: 1.0
Nodes (1): CreatePublicationRequest

### Community 81 - "Community 81"
Cohesion: 1.0
Nodes (1): UpdatePublicationRequest

### Community 86 - "Community 86"
Cohesion: 1.0
Nodes (1): SqidsConfig

### Community 87 - "Community 87"
Cohesion: 1.0
Nodes (1): CqrsBoundaryTest

## Knowledge Gaps
- **11 isolated node(s):** `MailArchiveNotifLog`, `MailArchiveNotif`, `ArchiveLocation`, `MessageTemplateRepository`, `AttachmentDownloadHistoryRepository` (+6 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 23`** (9 nodes): `GlobalExceptionHandler`, `.handleBadRequest()`, `.handleConflict()`, `.handleForbidden()`, `.handleGeneral()`, `.handleNotFound()`, `.handleUnauthorized()`, `.handleValidation()`, `GlobalExceptionHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 24`** (9 nodes): `MessageTemplateController`, `.create()`, `.delete()`, `.findAll()`, `.findAllList()`, `.findById()`, `.MessageTemplateController()`, `.update()`, `MessageTemplateController.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (9 nodes): `MailSignatureController`, `.getClientIp()`, `.signMail()`, `.verifySignature()`, `RateLimitService`, `.newBucket()`, `.resolveBucket()`, `MailSignatureController.java`, `RateLimitService.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 26`** (8 nodes): `ArchiveNumberParserUtil`, `.parseSequence()`, `ArchiveNumberParserUtilTest`, `.testInvalidPatterns()`, `.testParseLongRomanPattern()`, `.testParseShortPattern()`, `ArchiveNumberParserUtil.java`, `ArchiveNumberParserUtilTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 27`** (6 nodes): `PublicationCommandHandler`, `.create()`, `.delete()`, `.publish()`, `.update()`, `PublicationCommandHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (5 nodes): `MailTypeRepository`, `.existsByName()`, `.existsByNameAndIdNot()`, `.findAllByStatusOrderByIdAsc()`, `MailTypeRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (5 nodes): `MessageTemplateMapper`, `.toEntity()`, `.toResponse()`, `.updateEntity()`, `MessageTemplateMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 30`** (5 nodes): `AllowedFileTypeParams`, `.allowedSorts()`, `.defaultSort()`, `.toSpecification()`, `AllowedFileTypeParams.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 31`** (5 nodes): `OpenApiConfig`, `.coreApi()`, `.masterApi()`, `.openAPI()`, `OpenApiConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 32`** (5 nodes): `JooqConfig`, `.connectionProvider()`, `.dslContext()`, `.jooqConfiguration()`, `JooqConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 33`** (5 nodes): `MailArchiveNotifQueryService`, `.findById()`, `.findPending()`, `.MailArchiveNotifQueryService()`, `MailArchiveNotifQueryService.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (5 nodes): `PublicationQueryHandler`, `.download()`, `.findAll()`, `.findById()`, `PublicationQueryHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (4 nodes): `BooleanYesNoConverter.java`, `BooleanYesNoConverter`, `.convertToDatabaseColumn()`, `.convertToEntityAttribute()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 36`** (4 nodes): `MailResponseTime`, `.onCreate()`, `.onUpdate()`, `MailResponseTime.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 37`** (4 nodes): `MailOrgStatisticQueryRepository`, `.findByPeriodMonth()`, `.MailOrgStatisticQueryRepository()`, `MailOrgStatisticQueryRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 38`** (4 nodes): `MailOrgStatisticRepository`, `.findByCreatedByOrg()`, `.findByPeriodMonth()`, `MailOrgStatisticRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 39`** (4 nodes): `MailTypeMapper`, `.toLookup()`, `.toResponse()`, `MailTypeMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 40`** (4 nodes): `DocumentTypeMapper`, `.toLookup()`, `.toResponse()`, `DocumentTypeMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 41`** (4 nodes): `ArchiveMapper`, `.toAccessResponse()`, `.toResponse()`, `ArchiveMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 42`** (4 nodes): `MailNotificationListener`, `.onMailSent()`, `.sendNotificationToRecipient()`, `MailNotificationListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 43`** (4 nodes): `MailSentEventListener`, `.MailSentEventListener()`, `.onMailSent()`, `MailSentEventListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 44`** (4 nodes): `AsyncConfigurer`, `AsyncConfig`, `.getAsyncUncaughtExceptionHandler()`, `AsyncConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 45`** (4 nodes): `MailNumberGenerator`, `.generate()`, `.supports()`, `MailNumberGenerator.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 50`** (3 nodes): `MailServiceApplication`, `.main()`, `MailServiceApplication.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 51`** (3 nodes): `SqidEntity`, `.getId()`, `SqidEntity.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 52`** (3 nodes): `AttachmentDownloadHistory`, `.AttachmentDownloadHistory()`, `AttachmentDownloadHistory.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 53`** (3 nodes): `MailArchiveAccess`, `.create()`, `MailArchiveAccess.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 54`** (3 nodes): `MailActionLog`, `.MailActionLog()`, `MailActionLog.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 56`** (3 nodes): `MailResponseTimeQueryRepository`, `.MailResponseTimeQueryRepository()`, `MailResponseTimeQueryRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 57`** (3 nodes): `MailArchiveNotifRepository`, `.findByMailArchiveId()`, `MailArchiveNotifRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 58`** (3 nodes): `MailArchiveRepository`, `.findActiveById()`, `MailArchiveRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 59`** (3 nodes): `MailActionLogRepository`, `.findAllByMailIdOrderByCreatedAtDesc()`, `MailActionLogRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 60`** (3 nodes): `MailCategoryStatisticRepository`, `.findByPeriodMonthAndCategoryId()`, `MailCategoryStatisticRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 61`** (3 nodes): `QuickMessageMapper`, `.toResponse()`, `QuickMessageMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 62`** (3 nodes): `MailFolderMapper`, `.toResponse()`, `MailFolderMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 63`** (3 nodes): `PublicationMapper`, `.toDto()`, `PublicationMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 64`** (3 nodes): `PublicationNotificationListener`, `.onPublished()`, `PublicationNotificationListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 65`** (3 nodes): `MailStatisticListener`, `.onMailSent()`, `MailStatisticListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 66`** (3 nodes): `JacksonConfig`, `.objectMapper()`, `JacksonConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 67`** (3 nodes): `SecurityConfig`, `.filterChain()`, `SecurityConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 68`** (3 nodes): `AppWritePropertiesConfig`, `.appWriteProperties()`, `AppWritePropertiesConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 69`** (3 nodes): `WebClientConfig`, `.webClientBuilder()`, `WebClientConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 71`** (2 nodes): `MailArchiveNotifLog`, `MailArchiveNotifLog.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 72`** (2 nodes): `MailArchiveNotif`, `MailArchiveNotif.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 73`** (2 nodes): `ArchiveLocation`, `ArchiveLocation.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 74`** (2 nodes): `MessageTemplateRepository`, `MessageTemplateRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 75`** (2 nodes): `AttachmentDownloadHistoryRepository`, `AttachmentDownloadHistoryRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 77`** (2 nodes): `MailComponentDto`, `MailComponentDto.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 79`** (2 nodes): `RecipientComponentDto`, `RecipientComponentDto.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 80`** (2 nodes): `CreatePublicationRequest`, `CreatePublicationRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 81`** (2 nodes): `UpdatePublicationRequest`, `UpdatePublicationRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 86`** (2 nodes): `SqidsConfig`, `SqidsConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 87`** (2 nodes): `CqrsBoundaryTest`, `CqrsBoundaryTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `findById()` connect `Community 0` to `Community 1`, `Community 3`, `Community 4`, `Community 6`, `Community 7`, `Community 9`, `Community 11`, `Community 13`?**
  _High betweenness centrality (0.085) - this node is a cross-community bridge._
- **Why does `from()` connect `Community 2` to `Community 3`, `Community 4`, `Community 6`, `Community 7`, `Community 11`, `Community 17`, `Community 18`, `Community 19`, `Community 20`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **Are the 57 inferred relationships involving `findById()` (e.g. with `.onMailSent()` and `.create()`) actually correct?**
  _`findById()` has 57 INFERRED edges - model-reasoned connections that need verification._
- **Are the 42 inferred relationships involving `from()` (e.g. with `getRoles()` and `.findAll()`) actually correct?**
  _`from()` has 42 INFERRED edges - model-reasoned connections that need verification._
- **Are the 30 inferred relationships involving `userIdLong()` (e.g. with `.addRecipient()` and `.deleteRecipient()`) actually correct?**
  _`userIdLong()` has 30 INFERRED edges - model-reasoned connections that need verification._
- **What connects `MailArchiveNotifLog`, `MailArchiveNotif`, `ArchiveLocation` to the rest of the system?**
  _11 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.02 - nodes in this community are weakly interconnected._