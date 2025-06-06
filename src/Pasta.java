import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Pasta implements Serializable {
    private static final long serialUID = 1L;
    private String nome;
    private List<Arquivo> arquivos;
    private List<Pasta> subPastas;
    private Pasta parente;
    
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

    public void addArquivo(Arquivo arquivo){
        this.arquivos.add(arquivo);
    }

    public void removerArquivo(Arquivo arquivo){
        this.arquivos.remove(arquivo);
    }

    public void addSubPasta(Pasta pasta){
        this.subPastas.add(pasta);
        pasta.setParente(this);
    }
    public void removerSubPasta(Pasta pasta){
        this.subPastas.remove(pasta);
    }

    public Optional<Arquivo> encontrarArquivo(String nome){
        return arquivos.stream().filter(f -> f.getNome().equals(nome)).findFirst();
    }

    public Optional<Pasta> encontrarSubPasta(String nome){
        return subPastas.stream().filter(d -> d.getNome().equals(nome)).findFirst();
    }
    
  
    public void limparConteudoRecursivamente() {
        
        this.arquivos.clear();
        
        for (Pasta subPasta : new ArrayList<>(this.subPastas)) { 
            subPasta.limparConteudoRecursivamente(); 
            this.removerSubPasta(subPasta);
        }
    }

    @Override
    public String toString(){
        return "Pasta: "+ nome;
    }
}