package blockspace.article;

import model.*;
import blockspace.BlockSpace;
import blockspace.IBlockSpacePanel;
import blockspace.Block;
import stuff.OverridingDefaultClasses.TruMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class Article extends JPanel implements IBlockSpacePanel {

	private Block scroll = null;
	private AbstractHandler handler = null;

	private Helper modelHelper = new Helper(this);

	private List<Paragraph> parList = new ArrayList<>();

	final private static int SCROLL_BAR_WIDTH = /*25*/ 26;

	private static TruMap<Combo, ContextAction<Article>> actionMap = new TruMap<>();
	static {
		actionMap
			.p(new Combo(0, KeyEvent.VK_DOWN), mkAction(Article::focusNext).setCaption("Next Paragraph"))
			.p(new Combo(0, KeyEvent.VK_UP), mkAction(Article::focusBack).setCaption("Back Paragraph"))
			.p(new Combo(0, KeyEvent.VK_RIGHT), mkAction(Article::focusNext).setOmitMenuBar(true))
			.p(new Combo(0, KeyEvent.VK_DELETE), mkAction(Article::mergeNext).setCaption("Merge Next"))
			.p(new Combo(0, KeyEvent.VK_LEFT), mkAction(Article::focusBack).setOmitMenuBar(true))
			.p(new Combo(0, KeyEvent.VK_BACK_SPACE), mkAction(Article::mergeBack).setCaption("Merge Back"))
		;
	}

	public Article(BlockSpace parentBlockSpace) {
		super();
        this.setBackground(Color.DARK_GRAY);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		handler = new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() { return actionMap; }
		};

        addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				for (Paragraph par : parList) {
					fixParagraphWidth(par);
				}
				sukaSdelajNormalnijRazmer();
			}
		});

		scroll = parentBlockSpace.addModelChild(this);

		this.addNewParagraph();
	}

	private static ContextAction<Article> mkAction(Consumer<Article> lambda) {
		ContextAction<Article> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	/** @forbidden - you don't want change that method!!! these lines worth DOZENS OF HOURS */
	public void sukaSdelajNormalnijRazmer() {
		int width = scroll.getWidth() - SCROLL_BAR_WIDTH;
		int height = 0;
		for (Paragraph par: parList) {
			height += par.getPreferredSize().getHeight();
		}
		this.setMinimumSize(new Dimension(1, 1));
		this.setPreferredSize(new Dimension(width, height));
	}

	/** @forbidden - you don't want change that method!!! these lines worth DOZENS OF HOURS */
	public void fixParagraphWidth(Paragraph par) { // TODO: move to Paragraph
		int width = scroll.getWidth() - SCROLL_BAR_WIDTH;
		int height = par.getHeightIfWidthWas(width);
		par.setMinimumSize(new Dimension(1, 1));
		par.setPreferredSize(new Dimension(width, height));
	}

	public Paragraph addNewParagraph(int index) {
		Paragraph par = new Paragraph(this);

		// cuz this is wrapper of focused elements unlike ImagePanel and StaffPanel and can't be focused itself
		par.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) { getScroll().gotFocus(); }
			public void focusLost(FocusEvent e) { getScroll().lostFocus(); }
		});

		if (index > -1) { parList.add(index, par); }
		else { parList.add(par); }
		this.add(par, index);

		return par;
	}

	public Paragraph addNewParagraph() {
		return addNewParagraph(-1);
	}

	public Article removeParagraph(Paragraph par) {
		parList.remove(par);
		this.remove(par);
		return this;
	}

	private Article clearChildList() {
		this.removeAll();
        this.parList.clear();
		return this;
	}

	public List<Paragraph> getParList() { return parList; }

	@Override
	public Block getScroll() { return scroll; }
	@Override
	public Paragraph getFocusedChild() {
		Component focused = getModelParent().getModelParent().getWindow().getFocusOwner();
		return focused instanceof Paragraph && parList.contains(focused)
				? (Paragraph)focused
				: null;
	}
	@Override
	public Block getModelParent() { return getScroll(); }
	@Override
	public AbstractHandler getHandler() { return this.handler; }
	@Override
	public Helper getModelHelper() {
		return modelHelper;
	}

	@Override
	public JSONObject getJsonRepresentation() {
		return new JSONObject()
			.put("paragraphList", new JSONArray(getParList().stream().map(p -> p.getJsonRepresentation()).toArray()));
	}

	@Override
	public Article reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.clearChildList();

		JSONArray parJsonList = jsObject.getJSONArray("paragraphList");
		for (int idx = 0; idx < parJsonList.length(); ++idx) {
			JSONObject childJs = parJsonList.getJSONObject(idx);
			this.addNewParagraph().reconstructFromJson(childJs);
		}

		sukaSdelajNormalnijRazmer();
		return this;
	}

	private int getFocusedIndex() {
		return this.getFocusedChild() == null
				? -1
				: this.parList.indexOf(getFocusedChild());
	}

	private Article setFocusedIndex(int value) {
		if (parList.size() > 0) {
			int idx = limit(value, 0, parList.size() - 1);
			parList.get(idx).requestFocus();
		}
		return this;
	}

	// event handles

	public void focusNext() { this.setFocusedIndex(this.getFocusedIndex() + 1); }
	public void focusBack() { this.setFocusedIndex(this.getFocusedIndex() - 1); }

	public void mergeBack() {
		if (getFocusedIndex() > 0) {
			Paragraph back = getParList().get(getFocusedIndex() - 1);
			int caretPos = back.getText().length();
			getFocusedChild().mergeBackTo(back);
			back.requestFocus();
			back.setCaretPosition(caretPos);
		}
	}
	public void mergeNext() {
		if (getFocusedIndex() < getParList().size() - 1 && getFocusedIndex() > -1) {
			SwingUtilities.invokeLater(() -> {
				int caretPos = getFocusedChild().getText().length();
				Paragraph next = getParList().get(getFocusedIndex() + 1);
				next.mergeBackTo(getFocusedChild());
				getFocusedChild().setCaretPosition(caretPos);
			});
		}
	}
}
