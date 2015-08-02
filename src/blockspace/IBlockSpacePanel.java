package blockspace;

import model.IComponent;
import model.IModel;

public interface IBlockSpacePanel extends IComponent, IModel {

	Block getParentBlock();

}
