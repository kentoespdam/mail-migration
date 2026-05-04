# Graph Report - mail-service  (2026-05-04)

## Corpus Check
- 298 files · ~456,891 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1445 nodes · 2265 edges · 80 communities detected
- Extraction: 63% EXTRACTED · 37% INFERRED · 0% AMBIGUOUS · INFERRED: 835 edges (avg confidence: 0.8)
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
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 70|Community 70]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]
- [[_COMMUNITY_Community 73|Community 73]]
- [[_COMMUNITY_Community 74|Community 74]]
- [[_COMMUNITY_Community 76|Community 76]]
- [[_COMMUNITY_Community 77|Community 77]]
- [[_COMMUNITY_Community 78|Community 78]]
- [[_COMMUNITY_Community 79|Community 79]]
- [[_COMMUNITY_Community 81|Community 81]]
- [[_COMMUNITY_Community 83|Community 83]]
- [[_COMMUNITY_Community 84|Community 84]]
- [[_COMMUNITY_Community 85|Community 85]]
- [[_COMMUNITY_Community 90|Community 90]]
- [[_COMMUNITY_Community 91|Community 91]]

## God Nodes (most connected - your core abstractions)
1. `findById()` - 51 edges
2. `from()` - 39 edges
3. `userIdLong()` - 30 edges
4. `getDbValue()` - 23 edges
5. `resolve()` - 22 edges
6. `MailControllerTest` - 19 edges
7. `MailController` - 16 edges
8. `MailFolderControllerTest` - 15 edges
9. `PublicationFileStorageServiceTest` - 15 edges
10. `MailCommandService` - 13 edges

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
Cohesion: 0.03
Nodes (13): MailAttachmentController, MailController, MailControllerTest, MailFolderController, MailRecipientController, PublicationController, AllowedFileTypeController, DocumentTypeController (+5 more)

### Community 1 - "Community 1"
Cohesion: 0.03
Nodes (18): PagedRequest, CacheSerializationTest, MailQueryCacheListener, MailFolderQueryService, ArchiveQueryRepository, DocumentTypeQueryRepository, FolderCounterRepository, MailCategoryQueryRepository (+10 more)

### Community 2 - "Community 2"
Cohesion: 0.04
Nodes (15): MailArchiveQueryService, DocumentTypeCommandService, DocumentTypeCommandServiceTest, findById(), HrServiceClientFallback, DocumentTypeRepository, MailCategoryRepository, QuickMessageRepository (+7 more)

### Community 3 - "Community 3"
Cohesion: 0.04
Nodes (11): AttachmentFileStorageService, AttachmentFileStorageServiceTest, AttachmentService, AttachmentMapper, PageRequest, resolve(), PublicationQueryRepositoryTest, PublicationFileStorageService (+3 more)

### Community 4 - "Community 4"
Cohesion: 0.04
Nodes (17): ArchiveMapperImpl, ArchiveMapper, HasSqid, PrintLog, PrintLogTest, PublicationControllerTest, UserTask, PublicationNotifScheduler (+9 more)

### Community 5 - "Community 5"
Cohesion: 0.05
Nodes (11): MailFolderControllerTest, isMovable(), isPersonalFolder(), MailFolderCommandService, MailFolderRepository, AuditTrailService, MailCommandService, RecipientMapperImpl (+3 more)

### Community 6 - "Community 6"
Cohesion: 0.03
Nodes (23): SqidMapper, CacheConfig, CacheNames, CacheTtl, PageImplMixin, PageJacksonModule, PageRequestMixin, DocumentTypeMapperImpl (+15 more)

### Community 7 - "Community 7"
Cohesion: 0.06
Nodes (10): MailRecipientControllerTest, jabatanId(), jabatanNama(), getData(), HrServiceClient, HrServiceClient, MailRecipientRepository, MailSendService (+2 more)

### Community 8 - "Community 8"
Cohesion: 0.04
Nodes (12): AttachmentCommandService, AttachmentCommandServiceTest, AttachmentMapper, AttachmentMapperImpl, AttachmentQueryService, Attachment, Mail, MailArchive (+4 more)

### Community 9 - "Community 9"
Cohesion: 0.04
Nodes (11): MailFolder, MailFolderTest, Publication, AllowedFileType, DocumentType, MailCategory, MailCategoryTest, MailType (+3 more)

### Community 10 - "Community 10"
Cohesion: 0.05
Nodes (12): ArchiveReportRequest, ArchiveSearchRequest, DocumentTypeParams, MailFolderMailsParams, MailLookupParams, MailReportRequest, MailSearchRequest, MailCategoryParams (+4 more)

### Community 11 - "Community 11"
Cohesion: 0.07
Nodes (6): MailArchiveCommandService, ArchiveNumberGenerator, MailArchiveController, MailArchiveAccessRepository, MailArchiveNotifLogRepository, DefaultArchiveNumberGenerator

### Community 12 - "Community 12"
Cohesion: 0.09
Nodes (14): AllowedFileTypeDto, DocumentTypeLookup, DocumentTypeResponse, MailFolderLookup, HasSqid, MailResponse, MailSummaryResponse, MailCategoryLookup (+6 more)

### Community 13 - "Community 13"
Cohesion: 0.1
Nodes (7): AllowedFileTypeService, JpaPageRequest, JpaSearchRequest, AllowedFileTypeRepository, JpaPageRequest, PublicationMapperImpl, PublicationMapper

### Community 14 - "Community 14"
Cohesion: 0.08
Nodes (5): AbstractMailNumberGenerator, BmsMailNumberGenerator, BpnMailNumberGenerator, DefaultMailNumberGenerator, SmdMailNumberGenerator

### Community 15 - "Community 15"
Cohesion: 0.13
Nodes (4): MailRecipient, RecipientReadStatusListener, RecipientReadStatusListenerTest, RecipientMapper

### Community 16 - "Community 16"
Cohesion: 0.13
Nodes (4): MailThreadNode, MailThreadService, MailThreadServiceTest, MailTrackService

### Community 17 - "Community 17"
Cohesion: 0.13
Nodes (10): getValue(), OncePerRequestFilter, AppWriteAuthFilter, getRoles(), isAdmin(), isSystem(), toMailPrincipal(), from() (+2 more)

### Community 18 - "Community 18"
Cohesion: 0.15
Nodes (3): PublicationTest, QuickMessageTest, MailOrgStatistic

### Community 19 - "Community 19"
Cohesion: 0.17
Nodes (3): MailNumberGenerator, AbstractMailNumberGenerator, MailNumberGeneratorDelegator

### Community 20 - "Community 20"
Cohesion: 0.17
Nodes (3): MessageTemplateService, MessageTemplateMapperImpl, MessageTemplateMapper

### Community 21 - "Community 21"
Cohesion: 0.17
Nodes (4): PrintLogRepository, MailSignatureService, invalid(), valid()

### Community 22 - "Community 22"
Cohesion: 0.17
Nodes (3): MailArchiveAccessTest, fromValue(), CategoryStatusConverter

### Community 23 - "Community 23"
Cohesion: 0.18
Nodes (1): UserTaskRepository

### Community 24 - "Community 24"
Cohesion: 0.2
Nodes (6): ErrorDecoder, HrServiceConfig, HrServiceErrorDecoder, HrServiceException, RuntimeException, UnauthorizedException

### Community 25 - "Community 25"
Cohesion: 0.22
Nodes (1): GlobalExceptionHandler

### Community 26 - "Community 26"
Cohesion: 0.22
Nodes (1): MessageTemplateController

### Community 27 - "Community 27"
Cohesion: 0.25
Nodes (1): AttachmentController

### Community 28 - "Community 28"
Cohesion: 0.33
Nodes (1): PublicationCommandHandler

### Community 29 - "Community 29"
Cohesion: 0.4
Nodes (1): MailTypeRepository

### Community 30 - "Community 30"
Cohesion: 0.4
Nodes (1): MessageTemplateMapper

### Community 31 - "Community 31"
Cohesion: 0.4
Nodes (1): AllowedFileTypeParams

### Community 32 - "Community 32"
Cohesion: 0.4
Nodes (1): OpenApiConfig

### Community 33 - "Community 33"
Cohesion: 0.5
Nodes (1): JooqConfig

### Community 34 - "Community 34"
Cohesion: 0.4
Nodes (1): PublicationQueryHandler

### Community 35 - "Community 35"
Cohesion: 0.5
Nodes (1): BooleanYesNoConverter

### Community 36 - "Community 36"
Cohesion: 0.5
Nodes (1): MailArchiveNotif

### Community 37 - "Community 37"
Cohesion: 0.5
Nodes (1): MailResponseTime

### Community 38 - "Community 38"
Cohesion: 0.5
Nodes (1): MailArchiveNotifRepository

### Community 39 - "Community 39"
Cohesion: 0.5
Nodes (1): MailResponseTimeRepository

### Community 40 - "Community 40"
Cohesion: 0.5
Nodes (1): MailCategoryStatisticRepository

### Community 41 - "Community 41"
Cohesion: 0.5
Nodes (1): MailOrgStatisticRepository

### Community 42 - "Community 42"
Cohesion: 0.5
Nodes (1): MailTypeMapper

### Community 43 - "Community 43"
Cohesion: 0.5
Nodes (1): DocumentTypeMapper

### Community 44 - "Community 44"
Cohesion: 0.5
Nodes (1): ArchiveMapper

### Community 45 - "Community 45"
Cohesion: 0.5
Nodes (1): ArchivePublishedEventListener

### Community 46 - "Community 46"
Cohesion: 0.67
Nodes (1): MailNotificationListener

### Community 47 - "Community 47"
Cohesion: 0.5
Nodes (1): MailSentEventListener

### Community 48 - "Community 48"
Cohesion: 0.67
Nodes (2): AsyncConfigurer, AsyncConfig

### Community 49 - "Community 49"
Cohesion: 0.5
Nodes (1): MailNumberGenerator

### Community 54 - "Community 54"
Cohesion: 0.67
Nodes (1): MailServiceApplication

### Community 55 - "Community 55"
Cohesion: 0.67
Nodes (1): SqidEntity

### Community 56 - "Community 56"
Cohesion: 0.67
Nodes (1): MailArchiveNotifLog

### Community 57 - "Community 57"
Cohesion: 0.67
Nodes (1): AttachmentDownloadHistory

### Community 58 - "Community 58"
Cohesion: 0.67
Nodes (1): MailArchiveAccess

### Community 59 - "Community 59"
Cohesion: 0.67
Nodes (1): MailActionLog

### Community 60 - "Community 60"
Cohesion: 0.67
Nodes (1): MailCategoryStatistic

### Community 62 - "Community 62"
Cohesion: 0.67
Nodes (1): MailArchiveRepository

### Community 63 - "Community 63"
Cohesion: 0.67
Nodes (1): MailActionLogRepository

### Community 64 - "Community 64"
Cohesion: 0.67
Nodes (1): QuickMessageMapper

### Community 65 - "Community 65"
Cohesion: 0.67
Nodes (1): MailFolderMapper

### Community 66 - "Community 66"
Cohesion: 0.67
Nodes (1): PublicationMapper

### Community 67 - "Community 67"
Cohesion: 0.67
Nodes (1): MailResponseTimeListener

### Community 68 - "Community 68"
Cohesion: 0.67
Nodes (1): PublicationNotificationListener

### Community 69 - "Community 69"
Cohesion: 0.67
Nodes (1): MailStatisticListener

### Community 70 - "Community 70"
Cohesion: 0.67
Nodes (1): JacksonConfig

### Community 71 - "Community 71"
Cohesion: 0.67
Nodes (1): SecurityConfig

### Community 72 - "Community 72"
Cohesion: 0.67
Nodes (1): AppWritePropertiesConfig

### Community 73 - "Community 73"
Cohesion: 0.67
Nodes (1): WebClientConfig

### Community 74 - "Community 74"
Cohesion: 0.67
Nodes (1): ArchiveNumberGenerator

### Community 76 - "Community 76"
Cohesion: 1.0
Nodes (1): ArchiveLocation

### Community 77 - "Community 77"
Cohesion: 1.0
Nodes (1): MessageTemplateRepository

### Community 78 - "Community 78"
Cohesion: 1.0
Nodes (1): MailRepository

### Community 79 - "Community 79"
Cohesion: 1.0
Nodes (1): AttachmentDownloadHistoryRepository

### Community 81 - "Community 81"
Cohesion: 1.0
Nodes (1): MailComponentDto

### Community 83 - "Community 83"
Cohesion: 1.0
Nodes (1): RecipientComponentDto

### Community 84 - "Community 84"
Cohesion: 1.0
Nodes (1): CreatePublicationRequest

### Community 85 - "Community 85"
Cohesion: 1.0
Nodes (1): UpdatePublicationRequest

### Community 90 - "Community 90"
Cohesion: 1.0
Nodes (1): SqidsConfig

### Community 91 - "Community 91"
Cohesion: 1.0
Nodes (1): CqrsBoundaryTest

## Knowledge Gaps
- **10 isolated node(s):** `ArchiveLocation`, `MessageTemplateRepository`, `MailRepository`, `AttachmentDownloadHistoryRepository`, `MailComponentDto` (+5 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 23`** (11 nodes): `UserTaskRepository`, `.countByUserIdAndFolderIdAndReadStatus()`, `.emptyTrash()`, `.findActiveByUserIdAndMailId()`, `.findAllInTrashByMailId()`, `.findByUserIdAndMailId()`, `.purgeTrash()`, `.relocateMails()`, `.restoreFromTrash()`, `.updateFolder()`, `UserTaskRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (9 nodes): `GlobalExceptionHandler`, `.handleBadRequest()`, `.handleConflict()`, `.handleForbidden()`, `.handleGeneral()`, `.handleNotFound()`, `.handleUnauthorized()`, `.handleValidation()`, `GlobalExceptionHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 26`** (9 nodes): `MessageTemplateController`, `.create()`, `.delete()`, `.findAll()`, `.findAllList()`, `.findById()`, `.MessageTemplateController()`, `.update()`, `MessageTemplateController.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 27`** (8 nodes): `AttachmentController`, `.AttachmentController()`, `.delete()`, `.download()`, `.findById()`, `.findByOwner()`, `.upload()`, `AttachmentController.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (6 nodes): `PublicationCommandHandler`, `.create()`, `.delete()`, `.publish()`, `.update()`, `PublicationCommandHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (5 nodes): `MailTypeRepository`, `.existsByName()`, `.existsByNameAndIdNot()`, `.findAllByStatusOrderByIdAsc()`, `MailTypeRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 30`** (5 nodes): `MessageTemplateMapper`, `.toEntity()`, `.toResponse()`, `.updateEntity()`, `MessageTemplateMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 31`** (5 nodes): `AllowedFileTypeParams`, `.allowedSorts()`, `.defaultSort()`, `.toSpecification()`, `AllowedFileTypeParams.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 32`** (5 nodes): `OpenApiConfig`, `.coreApi()`, `.masterApi()`, `.openAPI()`, `OpenApiConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 33`** (5 nodes): `JooqConfig`, `.connectionProvider()`, `.dslContext()`, `.jooqConfiguration()`, `JooqConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (5 nodes): `PublicationQueryHandler`, `.download()`, `.findAll()`, `.findById()`, `PublicationQueryHandler.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (4 nodes): `BooleanYesNoConverter.java`, `BooleanYesNoConverter`, `.convertToDatabaseColumn()`, `.convertToEntityAttribute()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 36`** (4 nodes): `MailArchiveNotif`, `.onCreate()`, `.onUpdate()`, `MailArchiveNotif.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 37`** (4 nodes): `MailResponseTime`, `.onCreate()`, `.onUpdate()`, `MailResponseTime.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 38`** (4 nodes): `MailArchiveNotifRepository`, `.findByArchiveId()`, `.findByPosId()`, `MailArchiveNotifRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 39`** (4 nodes): `MailResponseTimeRepository`, `.findByOriginalMailId()`, `.findByReplyMailId()`, `MailResponseTimeRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 40`** (4 nodes): `MailCategoryStatisticRepository`, `.findByCategoryId()`, `.findByPeriodMonth()`, `MailCategoryStatisticRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 41`** (4 nodes): `MailOrgStatisticRepository`, `.findByOrganizationId()`, `.findByPeriodMonth()`, `MailOrgStatisticRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 42`** (4 nodes): `MailTypeMapper`, `.toLookup()`, `.toResponse()`, `MailTypeMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 43`** (4 nodes): `DocumentTypeMapper`, `.toLookup()`, `.toResponse()`, `DocumentTypeMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 44`** (4 nodes): `ArchiveMapper`, `.toAccessResponse()`, `.toResponse()`, `ArchiveMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 45`** (4 nodes): `ArchivePublishedEventListener`, `.ArchivePublishedEventListener()`, `.onArchivePublished()`, `ArchivePublishedEventListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 46`** (4 nodes): `MailNotificationListener`, `.onMailSent()`, `.sendNotificationToRecipient()`, `MailNotificationListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 47`** (4 nodes): `MailSentEventListener`, `.MailSentEventListener()`, `.onMailSent()`, `MailSentEventListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 48`** (4 nodes): `AsyncConfigurer`, `AsyncConfig`, `.getAsyncUncaughtExceptionHandler()`, `AsyncConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 49`** (4 nodes): `MailNumberGenerator`, `.generate()`, `.supports()`, `MailNumberGenerator.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 54`** (3 nodes): `MailServiceApplication`, `.main()`, `MailServiceApplication.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 55`** (3 nodes): `SqidEntity`, `.getId()`, `SqidEntity.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 56`** (3 nodes): `MailArchiveNotifLog`, `.onCreate()`, `MailArchiveNotifLog.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 57`** (3 nodes): `AttachmentDownloadHistory`, `.AttachmentDownloadHistory()`, `AttachmentDownloadHistory.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 58`** (3 nodes): `MailArchiveAccess`, `.create()`, `MailArchiveAccess.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 59`** (3 nodes): `MailActionLog`, `.MailActionLog()`, `MailActionLog.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 60`** (3 nodes): `MailCategoryStatistic.java`, `MailCategoryStatistic`, `.onUpdate()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 62`** (3 nodes): `MailArchiveRepository`, `.findActiveById()`, `MailArchiveRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 63`** (3 nodes): `MailActionLogRepository`, `.findAllByMailIdOrderByCreatedAtDesc()`, `MailActionLogRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 64`** (3 nodes): `QuickMessageMapper`, `.toResponse()`, `QuickMessageMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 65`** (3 nodes): `MailFolderMapper`, `.toResponse()`, `MailFolderMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 66`** (3 nodes): `PublicationMapper`, `.toDto()`, `PublicationMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 67`** (3 nodes): `MailResponseTimeListener`, `.onMailSent()`, `MailResponseTimeListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 68`** (3 nodes): `PublicationNotificationListener`, `.onPublished()`, `PublicationNotificationListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 69`** (3 nodes): `MailStatisticListener`, `.onMailSent()`, `MailStatisticListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 70`** (3 nodes): `JacksonConfig`, `.objectMapper()`, `JacksonConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 71`** (3 nodes): `SecurityConfig`, `.filterChain()`, `SecurityConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 72`** (3 nodes): `AppWritePropertiesConfig`, `.appWriteProperties()`, `AppWritePropertiesConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 73`** (3 nodes): `WebClientConfig`, `.webClientBuilder()`, `WebClientConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 74`** (3 nodes): `ArchiveNumberGenerator`, `.generate()`, `ArchiveNumberGenerator.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 76`** (2 nodes): `ArchiveLocation`, `ArchiveLocation.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 77`** (2 nodes): `MessageTemplateRepository`, `MessageTemplateRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 78`** (2 nodes): `MailRepository`, `MailRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 79`** (2 nodes): `AttachmentDownloadHistoryRepository`, `AttachmentDownloadHistoryRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 81`** (2 nodes): `MailComponentDto`, `MailComponentDto.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 83`** (2 nodes): `RecipientComponentDto`, `RecipientComponentDto.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 84`** (2 nodes): `CreatePublicationRequest`, `CreatePublicationRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 85`** (2 nodes): `UpdatePublicationRequest`, `UpdatePublicationRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 90`** (2 nodes): `SqidsConfig`, `SqidsConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 91`** (2 nodes): `CqrsBoundaryTest`, `CqrsBoundaryTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `findById()` connect `Community 2` to `Community 1`, `Community 3`, `Community 4`, `Community 5`, `Community 7`, `Community 11`, `Community 13`, `Community 21`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Why does `PageRequestMixin` connect `Community 6` to `Community 2`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Why does `from()` connect `Community 1` to `Community 3`, `Community 4`, `Community 8`, `Community 11`, `Community 16`, `Community 17`, `Community 19`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Are the 50 inferred relationships involving `findById()` (e.g. with `.create()` and `.update()`) actually correct?**
  _`findById()` has 50 INFERRED edges - model-reasoned connections that need verification._
- **Are the 38 inferred relationships involving `from()` (e.g. with `getRoles()` and `.findAll()`) actually correct?**
  _`from()` has 38 INFERRED edges - model-reasoned connections that need verification._
- **Are the 29 inferred relationships involving `userIdLong()` (e.g. with `.addRecipient()` and `.deleteRecipient()`) actually correct?**
  _`userIdLong()` has 29 INFERRED edges - model-reasoned connections that need verification._
- **Are the 22 inferred relationships involving `getDbValue()` (e.g. with `.convertToDatabaseColumn()` and `.MailRecipient()`) actually correct?**
  _`getDbValue()` has 22 INFERRED edges - model-reasoned connections that need verification._