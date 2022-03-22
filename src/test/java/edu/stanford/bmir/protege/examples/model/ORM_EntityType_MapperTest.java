package edu.stanford.bmir.protege.examples.model;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.vstu.nodelinkdiagram.ClientDiagramModel;
import org.vstu.nodelinkdiagram.MainDiagramModel;
import org.vstu.orm2diagram.model.ORM_DiagramFactory;
import org.vstu.orm2diagram.model.ORM_EntityType;
import org.vstu.orm2diagram.model.ORM_ValueType;

import java.io.File;


class ORM_EntityType_MapperTest {

    @Test
    void xxxxxxxxxx() {

//        MainDiagramModel mainModel = new MainDiagramModel(new ORM_DiagramFactory());
//        ClientDiagramModel clientModel = mainModel.registerClient(new Test_DiagramClient());
//
//        clientModel.beginUpdate();
//
//        ORM_EntityType et_Politician = clientModel.createNode(ORM_EntityType.class);
//        et_Politician.setName("Politician");
//
//        ORM_EntityType et_Country = clientModel.createNode(ORM_EntityType.class);
//        et_Country.setName("Country");
//
//        ORM_ValueType vt_CountryName = clientModel.createNode(ORM_ValueType.class);
//        vt_CountryName.setName("CountryName");
//
//        clientModel.commit();
//
//        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
//        IRI ontologyIRI = IRI.create("test_ontology");
//        OWLOntology ontology;
//
//        try {
//            ontology = owlManager.createOntology( ontologyIRI );
//            ORM_OWL_Mapper.ORM_to_OWL(clientModel, ontology);
//
//            File fileformated = new File("example.owl");
//            owlManager.saveOntology(ontology, new OWLXMLDocumentFormat(), IRI.create(fileformated.toURI()));
//
//        } catch (OWLOntologyCreationException e) {
//            e.printStackTrace();
//        } catch (OWLOntologyStorageException e) {
//            e.printStackTrace();
//        }
    }
}