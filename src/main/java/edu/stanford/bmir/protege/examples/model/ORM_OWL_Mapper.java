package edu.stanford.bmir.protege.examples.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vstu.nodelinkdiagram.ClientDiagramModel;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class ORM_OWL_Mapper {

    private static final Logger log = LoggerFactory.getLogger(ORM_OWL_Mapper.class);

    // Онтология пересоздается ЗАНОВО на основе диаграммы
    public static boolean ORM_to_OWL(ClientDiagramModel diagram, OWLOntology ontology) {

        boolean isOk = true;

        clearOntology(ontology);

        isOk = isOk && ORM_Element_Mapper.ORM_to_OWL(diagram, ontology);

        return isOk;
    }

    // Диаграмма пересоздается ЗАНОВО на основе онтологии
    public static boolean OWL_to_ORM(OWLOntology ontology, ClientDiagramModel diagram) {
        return false;
    }

    // Диаграмма ДОПОЛНЯЕТСЯ на основе порожденных знаний из онтологии
    public static boolean inferredOWLFragment_to_ORM(OWLOntology ontology, ClientDiagramModel diagram) {
        return false;
    }

    private static void clearOntology(OWLOntology ontology){
        ontology.getOWLOntologyManager().removeAxioms(ontology, ontology.getAxioms());
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static OWLOntologyManager manager;
    public static OWLOntology ontology;
    public static String ontology_iri;
    public static OWLEntityRemover entityRemover;
    public static OWLEntityRenamer owlEntityRenamer;


    public ORM_OWL_Mapper(OWLOntologyManager manager) {
        this.manager = manager;
        this.ontology = this.manager.getOntologies().iterator().next(); // Получаем текущую онтологию
        this.ontology_iri = this.ontology.getOntologyID().getOntologyIRI().get().toString() + '/';
        this.entityRemover = new OWLEntityRemover(Collections.singleton(this.ontology)); // Создаём специальный "удалитель"
        this.owlEntityRenamer = new OWLEntityRenamer(this.manager, this.manager.getOntologies()); // Создаём сменщика имён
    }

    public ORM_OWL_Mapper(String iri) throws Exception {
        this.manager = OWLManager.createOWLOntologyManager();
        this.ontology_iri = iri + '/';
        this.ontology = this.manager.createOntology(IRI.create(this.ontology_iri));
        this.entityRemover = new OWLEntityRemover(Collections.singleton(this.ontology));
        this.owlEntityRenamer = new OWLEntityRenamer(this.manager, this.manager.getOntologies()); // Создаём сменщика имён
    }

    /**
     * Для дебага
     */
    private static void debugSave() throws Exception {
        File fileformated = new File("example.owl");
        manager.saveOntology(ontology, new OWLXMLDocumentFormat(), IRI.create(fileformated.toURI()));
    }

    /**
     * Удаляет OWL-элемент из онтологии
     * @param owlEntity - удаляемый элемент
     */
    private void removeOWLEntity(OWLEntity owlEntity) {
        owlEntity.accept(entityRemover);
        manager.applyChanges(entityRemover.getChanges());
        entityRemover.reset();
    }

    /**
     * Возвращает множество OWL-классов, которые являются прямыми детьми
     * @return множество OWL-классов
     */
    private Set<OWLClass> getOWLThingSubClasses() {

        Set<OWLClass> subClasses = new HashSet<OWLClass>();
        Set<OWLClass> ontology_classes = ontology.getClassesInSignature(); // Получаем все OWL-классы в онтологии

        // Удаляем из множества сам OWLThing и Universe-класс
        ontology_classes.remove(df.getOWLThing());
        df.getOWLTopDataProperty();
        ontology_classes.remove(df.getOWLClass(IRI.create(ontology_iri + "Universe")));

        // Каждый класс, который НЕ имеет родителей, является прямым ребёнком OWLThing
        for (OWLClass owlClass : ontology_classes) {
            if (ontology.getSubClassAxiomsForSubClass(owlClass).size() == 0) {
                subClasses.add(owlClass);
            }
        }

        return subClasses;

    }

    /**
     * Возвращает множество OWL-классов, которые являются прямыми детьми указанного предка
     * @param classParent - предок
     * @return множество OWL-классов
     */
    private Set<OWLClass> getSubClasses(OWLClass classParent) {

        Set<OWLClass> subClasses = new HashSet<OWLClass>();

        // Для каждой subclass-аксиомы
        for (OWLSubClassOfAxiom subClassAxiom : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            // Если аксиома включает наш класс-предок, то добавляем класс-ребёнок из данной аксиомы в множество
            if (subClassAxiom.getSuperClass().asOWLClass().equals(classParent)) {
                subClasses.add(subClassAxiom.getSubClass().asOWLClass());
            }
        }
        //System.out.println(subClasses.toString());
        return subClasses;

    }

    /**
     * Создание universe-класса и disjoint'ов между классами
     */
    public void createCloseWorld() {
        // Получаем все классы онтологии 1-го уровня
        Set<OWLClass> ontology_classes = getOWLThingSubClasses();

        // Объявляем новые disjoint'ы между классами 1-уровня
        if (ontology_classes.size() > 1) {
            OWLDisjointClassesAxiom disjointClassesAxiom = df.getOWLDisjointClassesAxiom(ontology_classes);
            manager.addAxiom(ontology, disjointClassesAxiom);
        }

        // Объявляем, что OWL-Thing эквивалентен дизъюнкции (логическому ИЛИ) существующих классов
        OWLClassExpression universeClassExp = df.getOWLObjectUnionOf(ontology_classes);
        OWLEquivalentClassesAxiom universeClassAxiom = df.getOWLEquivalentClassesAxiom(df.getOWLThing(), universeClassExp);
        manager.addAxiom(ontology, universeClassAxiom);

    }
    /**
     * Обновление universe-класса и disjoint'ов между классами
     */
    // updateCloseWorld() -> createCloseWorld()
    private void updateCloseWorld() {

        // Получаем все 1-го уровня (прямые дети OWLThing) классы онтологии
        Set<OWLClass> ontology_classes = getOWLThingSubClasses();

        // Если в онтологии есть больше 1 класса, то удаляем текущий disjoint у классов
        if (ontology_classes.size() > 1) {
            boolean disjointIsRemove = false;
            for (OWLClass owlClass : ontology_classes) {
                for (OWLClassAxiom axiom : ontology.getAxioms(owlClass)) {
                    if (axiom.getAxiomType().toString().equals("DisjointClasses")) {
                        manager.removeAxiom(ontology, axiom);
                        disjointIsRemove = true;
                        break;
                    }
                }
                if (disjointIsRemove) {
                    break;
                }
            }
        }

        // Удаляем эквивалентность между OWLThing и классами 1-го уровня
        for (OWLClassAxiom axiom : ontology.getAxioms(df.getOWLThing())) {
            if (axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES) {
                manager.removeAxiom(ontology, axiom);
                break;
            }
        }

        // Объявляем новые disjoint'ы между классами 1-уровня
        if (ontology_classes.size() > 1) {
            OWLDisjointClassesAxiom disjointClassesAxiom = df.getOWLDisjointClassesAxiom(ontology_classes);
            manager.addAxiom(ontology, disjointClassesAxiom);
        }

        // Объявляем, что OWL-Thing эквивалентен дизъюнкции (логическому ИЛИ) существующих классов
        OWLClassExpression universeClassExp = df.getOWLObjectUnionOf(ontology_classes);
        OWLEquivalentClassesAxiom universeClassAxiom = df.getOWLEquivalentClassesAxiom(df.getOWLThing(), universeClassExp);
        manager.addAxiom(ontology, universeClassAxiom);

    }



    /**
     * Сохранение онтологии в файл
     * @param filename - название файла, в который будем сохранять онтологию
     */
    public void saveOntologyInFile(String filename) throws Exception {

        File fileformated = new File(filename);
        manager.saveOntology(ontology, new OWLXMLDocumentFormat(), IRI.create(fileformated.toURI()));

    }

    /**
    * Объявление Entity Type (OWL-класса)
    * @param entity_type_name - имя объявляемого класса
    */
    public void declareEntityType(String entity_type_name)  {

        // Объявление нового OWL-класса
        OWLClass owl_class = df.getOWLClass(IRI.create(ontology_iri + entity_type_name));
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(owl_class));

       // updateCloseWorld();
    }


    /**
     * Объявление Subtype
     * @param child_class_name - имя EntityType, который является дочерним
     * @param parent_class_name - имя EntityType, который является родителем
     */
    public void declareSubtype(String child_class_name, String parent_class_name) {

        // Объявляем subtype-аксиому между классами
        OWLClass child_owl_class = df.getOWLClass(IRI.create(ontology_iri + child_class_name));
        OWLClass parent_owl_class = df.getOWLClass(IRI.create(ontology_iri + parent_class_name));
        OWLSubClassOfAxiom subClassAxiom = df.getOWLSubClassOfAxiom(child_owl_class, parent_owl_class);
        manager.addAxiom(ontology, subClassAxiom);

        //updateCloseWorld();

    }


    /**
     * Объявление ValueType
     * @param value_name - имя DataProperty
     * @param datatype - тип значения
     * @param class_name - имя класса, который связан с dataProperty
     */
    public void declareValueType(String value_name, String datatype, String class_name)  {

        // Объявляем DataProperty
        OWLDataProperty valueType = df.getOWLDataProperty(IRI.create(ontology_iri + value_name + '.' + class_name));
        OWLDeclarationAxiom valueTypeDecl = df.getOWLDeclarationAxiom(valueType);
        manager.addAxiom(ontology, valueTypeDecl);

        // Добавляем класс в domains
        OWLClass owl_class = df.getOWLClass(IRI.create(ontology_iri + class_name));
        OWLDataPropertyDomainAxiom domainAxiom = df.getOWLDataPropertyDomainAxiom(valueType, owl_class);
        manager.addAxiom(ontology, domainAxiom);

        // Добавляем тип в ranges
        OWLDatatype stringDatatype = df.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
        OWLDataPropertyRangeAxiom rangeAxiom = df.getOWLDataPropertyRangeAxiom(valueType, stringDatatype);
        manager.addAxiom(ontology, rangeAxiom);

    }

    /**
    * Объявление Unary Role
    * @param role_name - имя унарной роли
    * @param class_name - имя класса, который играет роль
    */
    public void declareUnaryRole(String role_name, String class_name)  {

        // Объявляем DataProperty
        OWLDataProperty unaryRole = df.getOWLDataProperty(IRI.create(ontology_iri + role_name + '.' + class_name));
        OWLDeclarationAxiom unaryRoleDecl = df.getOWLDeclarationAxiom(unaryRole);
        manager.addAxiom(ontology, unaryRoleDecl);

        // Добавляем domains
        OWLClass owl_class = df.getOWLClass(IRI.create(ontology_iri + class_name));
        OWLDataPropertyDomainAxiom domainAxiom = df.getOWLDataPropertyDomainAxiom(unaryRole, owl_class);
        manager.addAxiom(ontology, domainAxiom);

        // Добавляем ranges
        OWLDatatype booleanDatatype = df.getBooleanOWLDatatype();
        OWLDataPropertyRangeAxiom rangeAxiom = df.getOWLDataPropertyRangeAxiom(unaryRole, booleanDatatype);
        manager.addAxiom(ontology, rangeAxiom);

    }


    /**
     * Объявление Binary Role
     * @param role_name - имя бинарной роли
     * @param inverse_role_name - имя инверсной бинарной роли
     * @param class_name - имя класса, который играет бинарную роль
     * @param inverse_class_name - имя класса, который играет инверсную бинарную роль
     */
    public void declareBinaryRole(String role_name, String inverse_role_name, String class_name, String inverse_class_name)  {

        // Создаём первую роль
        // Объявляем ObjectProperty
        OWLObjectProperty binaryRole = df.getOWLObjectProperty(IRI.create(ontology_iri + role_name + '.' + class_name + '.' + inverse_class_name));
        OWLDeclarationAxiom binaryRoleDecl = df.getOWLDeclarationAxiom(binaryRole);
        manager.addAxiom(ontology, binaryRoleDecl);

        // Добавляем domains
        OWLClass owl_class = df.getOWLClass(IRI.create(ontology_iri + class_name));
        OWLObjectPropertyDomainAxiom domainAxiom = df.getOWLObjectPropertyDomainAxiom(binaryRole, owl_class);
        manager.addAxiom(ontology, domainAxiom);

        // Добавляем ranges
        owl_class = df.getOWLClass(IRI.create(ontology_iri + inverse_class_name));
        OWLObjectPropertyRangeAxiom rangeAxiom = df.getOWLObjectPropertyRangeAxiom(binaryRole, owl_class);
        manager.addAxiom(ontology, rangeAxiom);


        // Создаём вторую (инверсную) роль
        // Объявляем ObjectProperty
        if (inverse_role_name.equals("")) {
            inverse_role_name = "inverse_" + role_name;
        }
        OWLObjectProperty inverseBinaryRole = df.getOWLObjectProperty(IRI.create(ontology_iri + inverse_role_name + '.' + inverse_class_name + '.' + class_name));
        OWLDeclarationAxiom inverseBinaryRoleDecl = df.getOWLDeclarationAxiom(inverseBinaryRole);
        manager.addAxiom(ontology, inverseBinaryRoleDecl);

        // Добавляем domains
        owl_class = df.getOWLClass(IRI.create(ontology_iri + inverse_class_name));
        domainAxiom = df.getOWLObjectPropertyDomainAxiom(inverseBinaryRole, owl_class);
        manager.addAxiom(ontology, domainAxiom);

        // Добавляем ranges
        owl_class = df.getOWLClass(IRI.create(ontology_iri + class_name));
        rangeAxiom = df.getOWLObjectPropertyRangeAxiom(inverseBinaryRole, owl_class);
        manager.addAxiom(ontology, rangeAxiom);


        // Объявляем, что роли инверсны друг другу
        OWLInverseObjectPropertiesAxiom inverseAxiom = df.getOWLInverseObjectPropertiesAxiom(binaryRole, inverseBinaryRole);
        manager.addAxiom(ontology, inverseAxiom);

    }

    // NEW METHODS
    public void clearAll(){
        manager.removeAxioms(ontology, ontology.getAxioms());
    }


}
