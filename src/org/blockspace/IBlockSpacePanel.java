package org.blockspace;

import org.klesun_model.IComponent;
import org.klesun_model.IModel;

public interface IBlockSpacePanel extends IComponent, IModel {

	Block getParentBlock();

}
