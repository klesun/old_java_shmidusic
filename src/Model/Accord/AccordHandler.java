
package Model.Accord;

import Model.Accord.Nota.Nota;
import java.awt.event.KeyEvent;

public class AccordHandler {
	
	private Accord context = null;
	
	public AccordHandler(Accord context) {
		this.context = context;
	}
	public void handleKey(KeyEvent e) {

//		if (getContext().getFocusedAccord() != null) {
//			new AccordHandler(getContext().getFocusedAccord()).handleKey(e);
//		}

		if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
			switch (e.getKeyCode()) {	
				case 't':case 'T': case 'Е': case 'е':
					getContext().triggerTuplets(3); // TODO: do something, so if child event handled - parent no need
					break;
				case KeyEvent.VK_DOWN:
					getContext().moveFocus(+1);
					getContext().requestNewSurface();
					break;
				case KeyEvent.VK_UP:
					getContext().moveFocus(-1);
					getContext().requestNewSurface();
					break;
				default: break;
			}
		} else if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_3) {
			if (getContext().getFocusedNota() == null) {
				for (Nota nota: getContext().getNotaList()) {
					nota.triggerIsSharp();
				}
			} else {
				getContext().getFocusedNota().triggerIsSharp(); // same here. may create dictionary<[KeyMask, KeyCode], Boolean> and pass from children, or just die(-100) in child, cause we have only one event. yes... die(-100) i like more
			}
			return;
		} 
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ADD:
				if (getContext().getFocusedIndex() == -1) {
					getContext().changeLength(1);
				} else {
					getContext().getFocusedNota().changeDur(1);
				}
				getContext().requestNewSurface();
				break;
			case KeyEvent.VK_SUBTRACT:
				if (getContext().getFocusedIndex() == -1) {
					getContext().changeLength(-1);
				} else {
					getContext().getFocusedNota().changeDur(-1);
				}
				getContext().requestNewSurface();
				break;
			case KeyEvent.VK_BACK_SPACE:
				String slog = getContext().getSlog();
				if (slog.length() < 2) {
					if (slog.length() == 0) {
						getContext().parentStaff.moveFocus(-1);
					}
					getContext().setSlog("");
				} else {
					getContext().setSlog(slog.substring(0, slog.length() - 1));
				}
				getContext().requestNewSurface();
				break;
			case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
			case KeyEvent.VK_NUMPAD0:case KeyEvent.VK_NUMPAD1:case KeyEvent.VK_NUMPAD2:case KeyEvent.VK_NUMPAD3:case KeyEvent.VK_NUMPAD4:
			case KeyEvent.VK_NUMPAD5:case KeyEvent.VK_NUMPAD6:case KeyEvent.VK_NUMPAD7:case KeyEvent.VK_NUMPAD8:case KeyEvent.VK_NUMPAD9:
				int cifra = (e.getKeyCode() >= '0' && e.getKeyCode() <= '9') ? e.getKeyCode() - '0' : e.getKeyCode() - KeyEvent.VK_NUMPAD0;
				Nota nota = getContext().getFocusedNota();
				if (nota != null) {
					if (nota.channel != cifra) {
						nota.setChannel(cifra);
					} else {
						getContext().setFocusedIndex(-1);
					}
				} else {
					cifra = Math.min(cifra, getContext().getNotaList().size());
					getContext().setFocusedIndex(cifra);
				}
				break;
			default:
				if (e.getKeyCode() >= 32 || e.getKeyCode() == 0) {
					// Это символ - напечатать
					getContext().setSlog(getContext().getSlog().concat("" + e.getKeyChar()));
				}
				break;
		}
	}

	public Accord getContext() {
		return this.context;
	}
}
