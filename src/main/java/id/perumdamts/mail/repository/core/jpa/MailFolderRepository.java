package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.MailFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MailFolderRepository extends JpaRepository<MailFolder, Long> {

    List<MailFolder> findByOwnerIdOrderByParentFolderIdAscIdAsc(Long ownerId);

    List<MailFolder> findByOwnerIdAndParentFolderId(Long ownerId, Long parentFolderId);

    boolean existsByOwnerIdAndName(Long ownerId, String name);

    boolean existsByOwnerIdAndNameAndIdNot(Long ownerId, String name, Long excludeId);

    @Query("SELECT f FROM MailFolder f WHERE f.parentFolderId = :parentId")
    List<MailFolder> findActiveChildren(@Param("parentId") Long parentId);
}
