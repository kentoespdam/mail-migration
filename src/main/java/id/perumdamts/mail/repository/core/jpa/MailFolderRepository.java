package id.perumdamts.mail.repository.core.jpa;

import id.perumdamts.mail.entity.core.PersonalFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MailFolderRepository extends JpaRepository<PersonalFolder, Integer> {

    List<PersonalFolder> findByOwnerIdOrderByParentFolderIdAscIdAsc(Integer ownerId);

    List<PersonalFolder> findByOwnerIdAndParentFolderId(Integer ownerId, Integer parentFolderId);

    boolean existsByOwnerIdAndName(Integer ownerId, String name);

    boolean existsByOwnerIdAndNameAndIdNot(Integer ownerId, String name, Integer excludeId);

    @Query("SELECT f FROM PersonalFolder f WHERE f.parentFolderId = :parentId")
    List<PersonalFolder> findActiveChildren(@Param("parentId") Integer parentId);
}
