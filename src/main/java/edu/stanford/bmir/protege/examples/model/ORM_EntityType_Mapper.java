package edu.stanford.bmir.protege.examples.model;

import org.semanticweb.owlapi.model.*;
import org.vstu.nodelinkdiagram.ClientDiagramModel;
import org.vstu.orm2diagram.model.ORM_EntityType;

import java.util.HashSet;
import java.util.Set;

public class ORM_EntityType_Mapper extends ORM_Element_Mapper {

    public static boolean ORM_to_OWL(ClientDiagramModel diagram, OWLOntology ontology) {

        boolean isOk = true;

        _ontologyManager = ontology.getOWLOntologyManager();
        _owlFactory = _ontologyManager.getOWLDataFactory();

        diagram.getElements(ORM_EntityType.class).forEach( et -> {
            entityTypeToOWL(et, ontology);
        });

        createCloseWorld(ontology);

        return isOk;
    }

    private static void entityTypeToOWL(ORM_EntityType et, OWLOntology ontology)  {

        OWLClass owl_class = _owlFactory.getOWLClass( createElementIRI(ontology, et.getName()) );
        _ontologyManager.addAxiom(ontology, _owlFactory.getOWLDeclarationAxiom(owl_class));
    }

    private static void createCloseWorld(OWLOntology ontology) {
        // Получаем все классы онтологии 1-го уровня
        Set<OWLClass> ontology_classes = getOWLThingSubClasses(ontology);

        // Объявляем новые disjoint'ы между классами 1-уровня
        if (ontology_classes.size() > 1) {
            OWLDisjointClassesAxiom disjointClassesAxiom = _owlFactory.getOWLDisjointClassesAxiom(ontology_classes);
            _ontologyManager.addAxiom(ontology, disjointClassesAxiom);
        }

        // Объявляем, что OWL-Thing эквивалентен дизъюнкции (логическому ИЛИ) существующих классов
        OWLClassExpression universeClassExp = _owlFactory.getOWLObjectUnionOf(ontology_classes);
        OWLEquivalentClassesAxiom universeClassAxiom =
                _owlFactory.getOWLEquivalentClassesAxiom(_owlFactory.getOWLThing(), universeClassExp);
        _ontologyManager.addAxiom(ontology, universeClassAxiom);
    }

    /** Возвращает множество OWL-классов, которые являются прямыми детьми */
    private static Set<OWLClass> getOWLThingSubClasses(OWLOntology ontology) {

        Set<OWLClass> subClasses = new HashSet<OWLClass>();
        Set<OWLClass> ontology_classes = ontology.getClassesInSignature(); // Получаем все OWL-классы в онтологии

        // Удаляем из множества сам OWLThing и Universe-класс
        ontology_classes.remove(_owlFactory.getOWLThing());
        _owlFactory.getOWLTopDataProperty();
        //ontology_classes.remove( _owlFactory.getOWLClass( createElementIRI(ontology, "Universe") ));

        // Каждый класс, который НЕ имеет родителей, является прямым ребёнком OWLThing
        for (OWLClass owlClass : ontology_classes) {
            if (ontology.getSubClassAxiomsForSubClass(owlClass).size() == 0) {
                subClasses.add(owlClass);
            }
        }

        return subClasses;
    }

    /** Возвращает множество OWL-классов, которые являются прямыми детьми указанного предка */
    private static Set<OWLClass> getSubClasses(OWLClass classParent, OWLOntology ontology) {

        Set<OWLClass> subClasses = new HashSet<OWLClass>();

        // Для каждой subclass-аксиомы
        for (OWLSubClassOfAxiom subClassAxiom : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            // Если аксиома включает наш класс-предок, то добавляем класс-ребёнок из данной аксиомы в множество
            if (subClassAxiom.getSuperClass().asOWLClass().equals(classParent)) {
                subClasses.add(subClassAxiom.getSubClass().asOWLClass());
            }
        }

        return subClasses;
    }
}
