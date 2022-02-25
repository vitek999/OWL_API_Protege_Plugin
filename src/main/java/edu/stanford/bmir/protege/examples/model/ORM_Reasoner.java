package edu.stanford.bmir.protege.examples.model;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.explanation.ExplanationResult;
import org.protege.editor.owl.ui.explanation.ExplanationService;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ORM_Reasoner {
    private OWLModelManager modelManager; // Встроенный менеджер онтологии
    private static final Logger log = LoggerFactory.getLogger(ORM_Reasoner.class);

    public ORM_Reasoner(OWLModelManager modelManager) {
        this.modelManager = modelManager;
    }

    // Проверка работы с reasoner
    public void getSubAxioms() {

        OWLReasoner reasoner = modelManager.getOWLReasonerManager().getCurrentReasoner();
//        log.info(reasoner.getReasonerName());
//        reasoner.getRootOntology().getAxioms(AxiomType.ANNOTATION_ASSERTION);

//        for (OWLSubClassOfAxiom subClassAxiom : reasoner.getRootOntology().ge) {
//            log.info("Hello");
//            log.info(subClassAxiom.toString());
//        }

//        ontology_manager.test();

        OWLOntology inferredOntology = modelManager.getOntologies().iterator().next();
        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        // we require all inferred stuff except for disjoints...
        gens.add(new InferredClassAssertionAxiomGenerator());
        gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        gens.add(new InferredEquivalentClassAxiomGenerator());
        gens.add(new InferredEquivalentDataPropertiesAxiomGenerator());
        gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
        gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
        gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        gens.add(new InferredPropertyAssertionGenerator());
        gens.add(new InferredSubClassAxiomGenerator());
        gens.add(new InferredSubDataPropertyAxiomGenerator());
        gens.add(new InferredSubObjectPropertyAxiomGenerator());
        // now create the target ontology and save
        OWLOntologyManager inferredManager = inferredOntology.getOWLOntologyManager();
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        //iog.fillOntology(OWLManager.getOWLDataFactory(), inferredOntology);

//        for (OWLSubClassOfAxiom subClassAxiom : inferredOntology.getAxioms(AxiomType.SUBCLASS_OF)) {
//            log.info("Hello");
//            log.info(subClassAxiom.toString());
//        }

//        for (OWLSubClassOfAxiom subClassAxiom : new HermitReasoningService().run(modelManager.getOntologies().iterator().next(),gens)) {
//            log.info("Hello");
//            log.info(subClassAxiom.toString());
//        }

        Set<OWLClass> classes = modelManager.getOntologies().iterator().next().getClassesInSignature();
        log.info("Print classes from model manager: ");
        log.info(classes.toString());

        for (OWLClass c : classes) {
            // the boolean argument specifies direct subclasses
            NodeSet<OWLClass> subClasses = reasoner.getSubClasses(c, true);
            for (OWLClass subClass : subClasses.getFlattened()) {
                log.info(subClass.getIRI().getShortForm() + " subclass of " + c.getIRI().getShortForm());
            }
        }

    }

    // Проверка reasoner и вывод ошибок, если есть
    public void checkOntology() throws OWLOntologyCreationException {

        OWLOntology o = modelManager.getOntologies().iterator().next();

        // Create a reasoner; it will include the imports closure
        OWLReasoner reasoner = modelManager.getOWLReasonerManager().getCurrentReasoner();
        // Ask the reasoner to precompute some inferences
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        List<InferredAxiomGenerator<? extends OWLAxiom>> gens =
                new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        gens.add(new InferredSubClassAxiomGenerator());


        OWLOntologyManager man = modelManager.getOWLOntologyManager();
        OWLOntology infOnt = man.createOntology();

        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        iog.fillOntology(man.getOWLDataFactory(), infOnt);

        // We can determine if the ontology is actually consistent
        log.info("" + reasoner.isConsistent());
        // get a list of unsatisfiable classes
        Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
        System.out.println("Unsatisfiable classes:");
        log.info("Unsatisfiable classes:");

        ExplanationManager explanationManager = modelManager.getExplanationManager();

        // leave owl:Nothing out
        for (OWLClass cls : bottomNode.getEntitiesMinusBottom()) {
            log.info("AXIOMS for: " + cls.getIRI().getShortForm());

            Set<OWLClassAxiom> axioms = infOnt.getAxioms(cls);
            Set<OWLClassAxiom> axioms2 = o.getAxioms(cls);
            log.info(axioms.toString());
            log.info(axioms2.toString());
            OWLClassAxiom axiom = axioms.stream().findFirst().get();
            log.info("has explanation: " + explanationManager.hasExplanation(axiom));

            ExplanationService explanationService = explanationManager.getExplainers().stream().findFirst().get();
            ExplanationResult result = explanationService.explain(axiom);
            log.info("plugin id of explanation: " + explanationService.getPluginId());
            log.info("explanation: " + result.toString());
            // log.info("has explanation: " + modelManager.getExplanationManager().handleExplain()
        }


    }
}
