package edu.stanford.bmir.protege.examples.view;

import java.awt.BorderLayout;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vstu.nodelinkdiagram.DiagramModel;
import org.vstu.nodelinkdiagram.DiagramNode;
import org.vstu.nodelinkdiagram.MainDiagramModel;
import org.vstu.orm2diagram.model.ORM_DiagramFactory;
import org.vstu.orm2diagram.model.ORM_Role;

public class ExampleViewComponent extends AbstractOWLViewComponent {

    private static final Logger log = LoggerFactory.getLogger(ExampleViewComponent.class);

    private ORM2_OWL2_Mapper_Panel ORM2OWL2MapperPanelComponent;

    @Override
    protected void initialiseOWLView() {
        setLayout(new BorderLayout());
        ORM2OWL2MapperPanelComponent = new ORM2_OWL2_Mapper_Panel(getOWLModelManager());
        ORM_DiagramFactory factory = new ORM_DiagramFactory();
        DiagramModel model = new MainDiagramModel(factory);
        DiagramNode node = factory.createNode(model, ORM_Role.class);
        log.info("SOUT: AAAAA: " + node);
        add(ORM2OWL2MapperPanelComponent, BorderLayout.CENTER);
        log.info("Example View Component initialized");
    }

	@Override
	protected void disposeOWLView() {
		ORM2OWL2MapperPanelComponent.dispose();
	}
}
