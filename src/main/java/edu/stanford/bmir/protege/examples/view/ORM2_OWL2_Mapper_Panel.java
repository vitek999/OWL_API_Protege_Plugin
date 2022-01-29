package edu.stanford.bmir.protege.examples.view;

import org.protege.editor.owl.model.OWLModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Класс, отвечающий за правую панель вкладки
 */
public class ORM2_OWL2_Mapper_Panel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(ORM2_OWL2_Mapper_Panel.class);

    // Элементы панели
    private JButton createOntButton = new JButton("Заполнить онтологию");
    private JButton createOntDocButton = new JButton("Заполнить онтологию doc");

    private ORM2_OWL2_Mapper ontology_manager; // Класс API

    public ORM2_OWL2_Mapper_Panel(OWLModelManager modelManager) {

        ontology_manager = new ORM2_OWL2_Mapper(modelManager.getOWLOntologyManager());

        createOntButton.addActionListener(e -> createExampleOntology());
        createOntDocButton.addActionListener(e -> createOntology());

        // Формирование панели
        JPanel jp = new JPanel();
        jp.setSize(1000, 1000);
        jp.add(createOntButton);
        jp.add(createOntDocButton);
        add(jp);
    }

    public void dispose()
    {}

    /**
     * Создание онтологии с помощью OWLAPI
     */
    private void createExampleOntology() {

        ontology_manager.clearAll();

        ontology_manager.declareEntityType("Person");
        ontology_manager.declareEntityType("Male");
        ontology_manager.declareEntityType("Female");

        ontology_manager.declareUnaryRole("is_sportsman", "Person");

        ontology_manager.declareValueType("has_gender", "", "Person");

        ontology_manager.declareBinaryRole("uses", "used_by", "Male", "Female");

        ontology_manager.createCloseWorld();
    }

    private void createOntology(){

        ontology_manager.clearAll();

        ontology_manager.declareEntityType("Person");
        ontology_manager.declareEntityType("Male");
        ontology_manager.declareEntityType("Female");
        ontology_manager.declareEntityType("Company");
        ontology_manager.declareEntityType("Car");

        ontology_manager.declareSubtype("Male", "Person");
        ontology_manager.declareSubtype("Female", "Person");

        ontology_manager.declareValueType("Gender", "xsd:string", "Person");

        ontology_manager.declareBinaryRole("WorksFor", "Employs", "Person", "Company");
        ontology_manager.declareBinaryRole("AffiliatedWith", "", "Person", "Company");
        ontology_manager.declareBinaryRole("Owns", "OwnedBy", "Company", "Car");
        ontology_manager.declareBinaryRole("Owns", "OwnedBy", "Person", "Car");

        ontology_manager.createCloseWorld();
    }

}
