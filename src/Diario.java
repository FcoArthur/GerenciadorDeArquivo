import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Diario implements Serializable {
    //falta implementar 
    //o journal e um "arquivo" que guarda alterações podendo ser de vairos tipos 
    //estou dividindo em dois um que guarda a entrada e outro que possui o conjunto 
    //tipo o conteudo de jarro e o jarro
    //o arquivo e bem intuitivo e um arquivo tendo nome/conteudo
    //a pasta tambem e +- intuitivo pois alem do nome guarda tambem os arquvios(em uma lista)
    //guarda tambem subpastas(em uma lista) e tem que ter sua relação com a subpasta
    //alem de metodos como localizar pasta/arquivo e adicionar/remover pasta/arquivo
    //depos disso tudo ter sido definido fui fazer o entryJournal ou entrada no diario
    //o journal e um diario duh
    //no entradaNoDiario eu defini as operações
    //em um enum chamado tipo de operaçoes
    //e o entradanodiario possui um caminho para saber onde o arquivo vai ser colocado(n e ele que coloca o arquivo)
    //ele so sabe onde foi colocado)
    //o novoCaminho para ser um ctrl+c onde eu vou colocar o arquivo ou pasta nova 
    // ou como um f2 para renomear que tem a mesma ideia pois e mais facil pegar ele e colocar nele mesmo com outro nome 
    // e o conteudo de um arquivo que bem... e o conteudo do arquivo 
    // arthur do futuro tudo ja está com o toString feito ent n precisa ir checando essa parte    
    // a n ser que você tenha errado algo de portugues
     private static final long serialUID = 1L;
     private List<EntradaNoDiario> diario;

     public Diario(){
        this.diario = new ArrayList<>();
     }

     public void addDiario(EntradaNoDiario e){
        this.diario.add(e);
     }

     public List<EntradaNoDiario> getDiarios(){
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