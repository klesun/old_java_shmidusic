package Model;

public interface IHandlerContext {
	AbstractModel getFocusedChild();
	IHandlerContext getModelParent();
}
