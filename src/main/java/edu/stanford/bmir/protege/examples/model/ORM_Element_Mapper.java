package edu.stanford.bmir.protege.examples.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.vstu.nodelinkdiagram.ClientDiagramModel;

public class ORM_Element_Mapper {

    protected static OWLOntologyManager _ontologyManager;
    protected static OWLDataFactory _owlFactory;

    // Все элементы диаграммы пересоздаются ЗАНОВО в онтологии
    public static boolean ORM_to_OWL(ClientDiagramModel diagram, OWLOntology ontology) {
        return false;
    }

    // Все элементы из онтологии пересоздаются ЗАНОВО в диаграмме
    public static boolean OWL_to_ORM(OWLOntology ontology, ClientDiagramModel diagram) {
        return false;
    }

    // Диаграмма ДОПОЛНЯЕТСЯ элементами, порожденными в онтологии
    public static boolean inferredOWLFragment_to_ORM(OWLOntology ontology, ClientDiagramModel diagram) {
        return false;
    }

    protected static IRI createElementIRI(OWLOntology ontology, String elementName) {
        return IRI.create( ontology.getOntologyID().getOntologyIRI().get().toString() + '/' + elementName );
    }
}
