package schemecalc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author muraokadaisuke
 */
public class mainController implements Initializable
{
    @FXML
    TextField textField;
    
    private static SInterpreter interpreter;
    public static SInterpreter getInterpreter() { return interpreter; }
    
    private String Calculate()
    {
    	FormulaParser fp = new FormulaParser();
    	String str = textField.getText();
    	SNumber ans = fp.calcFormula(str, interpreter);
    	return (Util.occuredError() ? str + "=Error" : str + "=" + ans.toString());
    }
    
    @FXML
    private void calc_onAction(ActionEvent e)
    {
    	textField.setText(Calculate());
    }
    
    @FXML
    private void onKeyPressed(KeyEvent key)
    {
    	if (key.getCode() == KeyCode.ENTER)
    		textField.setText(Calculate());
    }
    
    @FXML
    private void key_onAction(ActionEvent e)
    {
    	Button button = (Button)e.getSource();
    	textField.appendText(button.getText().equals("x") ? "*" : button.getText());
    }
    
    @FXML
    private void clear_onAction(ActionEvent e)
    {
    	textField.setText("");
    }
    
    @FXML
    private void reset_onAction(ActionEvent e)
    {
    	interpreter = new SInterpreter();
    }
    
    @FXML
    private void load_onAction(ActionEvent e)
    {
    	FileChooser fc = new FileChooser();
    	fc.setTitle("Scheme???????????????????????????");
    	File ret = fc.showOpenDialog(null);
    	if (ret == null)
    		return;
    	
    	SParser parser = new SParser();
    	FileInputStream fileStream = null;
    	Reader reader;
    	try
		{
			fileStream = new FileInputStream(ret);
			reader = new InputStreamReader(fileStream);
		}
		catch (Exception e2)
		{
			textField.setText("????????????????????????????????????????????????");
			return;
		}
    	
    	try
		{
    		List<SAtom> parseList = parser.parse(reader);
    		for (SAtom atom : parseList)
    		{
    			interpreter.eval(atom);
    		}
		}
		finally
		{
			try
			{
				fileStream.close();
				reader.close();
			}
			catch (IOException e1)
			{
			}
		}
    }
    
    @FXML
    private void Interactive_onAction(ActionEvent e)
    {
    	Stage consoleStage = new Stage();
    	Parent consoleParent = null;
    	try
		{
			consoleParent = FXMLLoader.load(FxCalc.class.getResource("console.fxml"));
		}
		catch (IOException e1)
		{
			return;
		}
    	Scene consoleScene = new Scene(consoleParent);
    	consoleStage.setScene(consoleScene);
    	consoleStage.show();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    	interpreter = new SInterpreter();
    }
}
