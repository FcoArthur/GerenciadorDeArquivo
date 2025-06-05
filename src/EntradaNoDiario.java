import java.io.Serializable;

public class EntradaNoDiario implements Serializable {
    private static final long serialUID = 1L;
    public enum TipoDeOperacao{
        CRIAR_ARQUIVO, DELETAR_ARQUIVO, RENOMEAR_ARQUIVO,
        CRIAR_PASTA, DELETAR_PASTA, RENOMEAR_PASTA,
        COPIAR_ARQUIVO
    }

    private TipoDeOperacao tipo;
    private String caminho; //caminho do arquivo
    private String novoCaminho; //renomear/copiar
    private String conteudo; // criar arquivo

    public EntradaNoDiario(TipoDeOperacao tipo, String caminho) {
        this.tipo = tipo;
        this.caminho = caminho;
    }

     public EntradaNoDiario(TipoDeOperacao tipo, String caminho, String novoCaminho) {
        this(tipo,caminho);
        this.novoCaminho = novoCaminho;
    }
     public EntradaNoDiario(TipoDeOperacao tipo, String caminho, String novoCaminho, String conteudo) {
        this(tipo,caminho,novoCaminho);
        this.conteudo = conteudo;
    }

     public TipoDeOperacao getTipo() {
         return tipo;
     }

     public String getCaminho() {
         return caminho;
     }

     public String getNovoCaminho() {
         return novoCaminho;
     }

     public String getConteudo() {
         return conteudo;
     }
    
    @Override
    public String toString(){
        String base = "Operacao: " + tipo + ", Caminho "+ caminho;
        if(novoCaminho != null){
            base += ", Novo Caminho/Nome " + novoCaminho;
        }
        if(conteudo !=null){
            base += ", conteudo '" + conteudo + "'";
        }
        return base;
    }

    
}