package id.perumdamts.mail;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "id.perumdamts.mail", importOptions = ImportOption.DoNotIncludeTests.class)
public class CqrsBoundaryTest {

    @ArchTest
    static final ArchRule publication_query_services_should_not_depend_on_jpa_repositories =
            noClasses().that().resideInAPackage("..service.core.publication..")
                    .and().haveSimpleNameContaining("QueryService")
                    .should().dependOnClassesThat().resideInAPackage("..repository..jpa..");

    @ArchTest
    static final ArchRule publication_command_services_should_not_depend_on_jooq_repositories =
            noClasses().that().resideInAPackage("..service.core.publication..")
                    .and().haveSimpleNameContaining("CommandService")
                    .should().dependOnClassesThat().resideInAPackage("..repository..jooq..");
}
