package lib;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 *
 * @author Orlando
 */
public class ReadFile {
    private String file;

    public ReadFile(String file){
        this.setFile(getDiretorio() + file);
    }
    
    public String getConfig(String config) throws FileNotFoundException {
        String retorno = "";
        Scanner leitura = new Scanner(this.getFile());
        while(leitura.hasNextLine()) {
            String[] StrFile = leitura.nextLine().toLowerCase().split("=");
            if(!StrFile[0].trim().equals("")) {
                if(StrFile[0].trim().equals("["+config+"]")) {
                    retorno = StrFile[1].replace(";", "").trim();
                    break;
                }
            }
        }
        leitura.close();
        return retorno;
    }
    
    public InputStream getFile() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public void setFile(String file) {
        this.file = file;
    }
    
    public String getDiretorio(){
        String caminho = "";
        try {
            caminho = ReadFile.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            caminho = caminho.substring(1, caminho.lastIndexOf('/') + 1);
        }
        catch(URISyntaxException ex) {
            ex.printStackTrace();
        }
        return caminho;
    }
    
    public Scanner leArquivo() throws FileNotFoundException{
        return new Scanner(this.getFile());
    }
    
    public String getStringArquivo() throws FileNotFoundException {
        Scanner leitura = new Scanner(this.getFile());
        String StrFile = "";
        while(leitura.hasNextLine()) {
            StrFile += leitura.nextLine();
        }
        return StrFile;
    }
}