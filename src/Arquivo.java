import java.io.Serializable;

public class Arquivo implements Serializable {
    private String nome;
    private String conteudo;
    private static final long serialUID = 1L;

    public Arquivo(String nome, String conteudo) {
        this.nome = nome;
        this.conteudo = conteudo;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getConteudo() {
        return conteudo;
    }
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    @Override
    public String toString(){
        return "Arquivo: " + nome + " (conteudo: '" + conteudo + "'";
    }

    
}
