package client;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class MyCellRenderer extends JLabel implements ListCellRenderer
{

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	@Override
	public Component getListCellRendererComponent(JList list, // the list
			Object value, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // does the cell have focus
	{
		setText((String) value);
		if (index < 2)
			setForeground(Color.red);
		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}
