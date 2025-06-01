import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Pasta implements Serializable {
    private static final long serialUID = 1L;
    private String nome;//nome da pasta
    private List<Arquivo> arquivos;//lista de Arquivos dentro da pasta
    private List<Pasta> subPastas;//lista de subpastas dentro da pasta (e uma pasta)
    private Pasta parente;//relação para a subpasta e pasta
    
    public Pasta(String nome) {
        this.nome = nome;
        this.arquivos = new ArrayList<>();
        this.subPastas = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public List<Arquivo> getArquivos() {
        return arquivos;
    }
    public List<Pasta> getSubPastas() {
        return subPastas;
    }
    public Pasta getParente() {
        return parente;
    }
    public void setParente(Pasta parente) {
        this.parente = parente;
    }

    //metodos kk...

    //adicionar um arquivo em uma pasta
    public void addArquivo(Arquivo arquivo){
        this.arquivos.add(arquivo);
    }

    //remover um arquivo da pasta
    public void removerArquivo(Arquivo arquivo){
        this.arquivos.remove(arquivo);
    }

    //adicionar uma subpasta na pasta
    public void addSubPasta(Pasta pasta){
        this.subPastas.add(pasta);
        pasta.setParente(this);
    }
    //remover uma subpasta
    public void removerSubPasta(Pasta pasta){
        this.subPastas.remove(pasta);
    }

    //encontrar arquivo na pasta
    public Optional<Arquivo> encontrarArquivo(String nome){
        return arquivos.stream().filter(f -> f.getNome().equals(nome)).findFirst();
    }

    //encontrar subpasta na pasta
    public Optional<Pasta> encontrarSubPasta(String nome){
        return subPastas.stream().filter(d -> d.getNome().equals(nome)).findFirst();
    }
    
    @Override
    public String toString(){
        return "Pasta: "+ nome;
    }
}
