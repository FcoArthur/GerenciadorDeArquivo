import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Diario implements Serializable {
     private static final long serialUID = 1L;
     private List<String> diario;

     public Diario(){
        this.diario = new ArrayList<>();
     }

     public void addDiario(String entrada){
        this.diario.add(entrada);
     }

     public List<String> getDiarios(){
        return diario;
     }

     public void limpar(){
        this.diario.clear();
     }

     @Override
     public String toString(){
        return "diario de operações: " + diario.size() + " entradas.";
     }
}