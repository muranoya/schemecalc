package schemecalc;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConsoleController implements Initializable
{
	@FXML
	private TextArea output;
	@FXML
	private TextField input;
    
	@FXML
	private void input_onKeyPressed(KeyEvent e)
	{
		if (e.getCode() == KeyCode.ENTER)
		{
			eval_onAction(null);
		}
	}
	
	@FXML
	private void eval_onAction(ActionEvent e)
	{
		SParser parser = new SParser();
		List<SAtom> parseList = parser.parse(input.getText());
		for (SAtom atom : parseList)
		{
			output.appendText("$ " + atom.toString() + "\n");
			output.appendText(">" + mainController.getInterpreter().eval(atom).toString() + "\n");
		}
		input.setText("");
	}
	
	@Override
    public void initialize(URL url, ResourceBundle rb)
    {
		output.setEditable(false);
    }
}
