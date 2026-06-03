package id.perumdamts.mail;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "id.perumdamts.mail", importOptions = ImportOption.DoNotIncludeTests.class)
public class SqidSeamArchTest {

    @ArchTest
    static final ArchRule controllers_and_mappers_should_not_depend_on_sqids_encoder =
            noClasses().that().resideInAnyPackage("..controller..", "..dto..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("id.perumdamts.mail.util.SqidsEncoder");

}
