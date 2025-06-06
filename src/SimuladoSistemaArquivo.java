import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SimuladoSistemaArquivo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Pasta root;
    private Pasta pastaAtual;
    private Diario diario;
    private static final String ESTADODOARQUIVODOSISTEMA_ARQUIVO = "arquivodosistema_estado.ser";
    private static final String LOG_JORNAL_LEGIVEL = "log_jornal.txt";

    public SimuladoSistemaArquivo() {
        this.root = new Pasta("root");
        this.pastaAtual = root;
        this.diario = new Diario();
        carregarEstado();
    }

    private void escreverLogLegivel(String mensagem) {
        try (FileWriter fw = new FileWriter(LOG_JORNAL_LEGIVEL, true);
             PrintWriter pw = new PrintWriter(fw)) {
            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            pw.println(agora.format(formatador) + " - " + mensagem);
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log TXT: " + e.getMessage());
        }
    }

    private void salvarEstado(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ESTADODOARQUIVODOSISTEMA_ARQUIVO))){
            oos.writeObject(this.root);
        }catch(IOException e){
            System.err.println("Erro ao salvar estado do sistema de arquivos: "+ e.getMessage());
        }
    }
    
    private void carregarEstado(){
        File estadoDoArquivo = new File(ESTADODOARQUIVODOSISTEMA_ARQUIVO);
        if(estadoDoArquivo.exists()){
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(estadoDoArquivo))) {
                this.root = (Pasta) ois.readObject();
                this.pastaAtual = root;
                System.out.println("Estado do sistema de arquivos carregado.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao carregar estado do sistema de arquivos: " + e.getMessage());
                System.out.println("Iniciando um novo sistema de arquivos.");
                this.root = new Pasta("root");
                this.pastaAtual = root;
            }
        } else {
            System.out.println("Nenhum estado do sistema de arquivos existente. Criando um novo.");
        }
    }

    public void mudarPasta(String path){
        if(path.equals("/")){
            pastaAtual=root;
            System.out.println("Diretório atual: /");
            return;
        }
        if(path.equals("..")){
            if(pastaAtual.getParente() != null){
                pastaAtual = pastaAtual.getParente();
                System.out.println("Diretório atual: "+getPathAtual());
            }else{
                System.out.println("Já está na raiz.");
            }
            return;
        }

        Optional<Pasta> pastaAlvo = pastaAtual.encontrarSubPasta(path);
        if(pastaAlvo.isPresent()){
            pastaAtual = pastaAlvo.get();
            System.out.println("Diretório atual: "+getPathAtual());
        }else{
            System.out.println("Diretório '"+path+"' não encontrado.");
        }
    }

    public String getPathAtual(){
        if(pastaAtual == root){
            return "/";
        }
        StringBuilder pathBuilder = new StringBuilder();
        Pasta temp = pastaAtual;
        while (temp != null && temp != root) {
            pathBuilder.insert(0,"/"+temp.getNome());
            temp = temp.getParente();
        }
        return pathBuilder.toString();
    }

    private Optional<Pasta> getPastaPorCaminho(String path){
        if(path.equals("/")){
            return Optional.of(root);
        }
        String[] partesPath = path.substring(1).split("/");
        Pasta atual = root;
        for(String part : partesPath){
            if(part.isEmpty()) continue;
            Optional<Pasta> proximoDiretorio = atual.encontrarSubPasta(part);
            if(proximoDiretorio.isPresent()){
                atual = proximoDiretorio.get();
            }else{
                return Optional.empty();
            }
        }
        return Optional.of(atual);
    }

    public void criarArquivo(String nome, String conteudo){
        if(pastaAtual.encontrarArquivo(nome).isPresent()){
            System.out.println("Erro: Arquivo '"+nome+"' já existe no diretório.");
            return;
        }
        if(pastaAtual.encontrarSubPasta(nome).isPresent()){
            System.out.println("Erro: Já existe uma pasta com o nome '"+nome+"'.");
            return;
        }

        String operacao = "CRIAR_ARQUIVO";
        String local = getPathAtual();
        String nomeCriado = nome;
        String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome do arquivo " + nomeCriado;
        diario.addDiario(logMessage);
        escreverLogLegivel(logMessage);
        salvarEstado();

        pastaAtual.addArquivo(new Arquivo(nome, conteudo));
        System.out.println("Arquivo '"+nome+"' criado com sucesso.");
        diario.limpar();
        salvarEstado();
    }

    public void deletarArquivo(String nome){
        Optional<Arquivo> arquivoParaDeletar = pastaAtual.encontrarArquivo(nome);
        if(!arquivoParaDeletar.isPresent()){
            System.out.println("Erro: Arquivo '"+nome+"' não localizado.");
            return;
        }
        String operacao = "DELETAR_ARQUIVO";
        String local = getPathAtual();
        String nomeExcluido = nome;
        String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome do arquivo " + nomeExcluido;
        diario.addDiario(logMessage);
        escreverLogLegivel(logMessage);
        salvarEstado();

        pastaAtual.removerArquivo(arquivoParaDeletar.get());
        System.out.println("Arquivo '"+nome+"' apagado com sucesso.");

        diario.limpar();
        salvarEstado();
    }

    public void renomearArquivo(String nomeVelho,String nomeNovo){
        Optional<Arquivo> arquivoParaRenomear = pastaAtual.encontrarArquivo(nomeVelho);
        if(!arquivoParaRenomear.isPresent()){
            System.out.println("Erro: Arquivo '"+nomeVelho+"' não encontrado.");
            return;
        }
        if(pastaAtual.encontrarArquivo(nomeNovo).isPresent() || pastaAtual.encontrarSubPasta(nomeNovo).isPresent()){
            System.out.println("Erro: Já existe pasta/diretório com o nome '"+ nomeNovo+"'.");
            return;
        }
        String operacao = "RENOMEAR_ARQUIVO";
        String local = getPathAtual();
        String nomeAntigo = nomeVelho; 
        String nomeNovoLog = nomeNovo;
        String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome do arquivo (de/para) " + nomeAntigo + "/" + nomeNovoLog;
        diario.addDiario(logMessage);
        escreverLogLegivel(logMessage);
        salvarEstado();

        arquivoParaRenomear.get().setNome(nomeNovo);
        System.out.println("Arquivo '"+nomeVelho+"' renomeado para '"+nomeNovo+"'.");
        diario.limpar();
        salvarEstado();
    }

    public void copiarArquivo(String arquivoFonteCaminho, String pastaDestino){
        String[] partesFonte = arquivoFonteCaminho.split("/");
        String nomeDoArquivoFonte = partesFonte[partesFonte.length-1];
        String caminhoPastaFonte = (partesFonte.length>1)? String.join("/",Arrays.copyOf(partesFonte,partesFonte.length-1)):"";
        
        Pasta fontePasta = null;
        if(caminhoPastaFonte.isEmpty() || caminhoPastaFonte.equals(".")){
            fontePasta = pastaAtual;
        }else if(caminhoPastaFonte.equals("/")){
            fontePasta = root;
        }else{
            fontePasta = getPastaPorCaminho(caminhoPastaFonte).orElse(null);
        }

        if(fontePasta == null){
            System.out.println("Erro: Diretório de origem '"+ caminhoPastaFonte +"' não existe.");
            return;
        }

        Optional<Arquivo> arquivoParaCopiarOptional = fontePasta.encontrarArquivo(nomeDoArquivoFonte);
        if(!arquivoParaCopiarOptional.isPresent()){
            System.out.println("Erro: Arquivo de origem '"+nomeDoArquivoFonte+"' não encontrado em '"+arquivoFonteCaminho+"'.");
            return;
        }
        Arquivo arquivoParaCopiar = arquivoParaCopiarOptional.get();

        Pasta destinoPasta = getPastaPorCaminho(pastaDestino).orElse(null);
        if (destinoPasta==null) {
            System.out.println("Erro: Diretório de destino '"+ pastaDestino +"' não encontrado.");
            return;
        }
        if (destinoPasta.encontrarArquivo(arquivoParaCopiar.getNome()).isPresent()){
            System.out.println("Erro: Já existe um arquivo com o nome '"+arquivoParaCopiar.getNome()+"' no destino.");
            return;
        }

        String operacao = "COPIAR_ARQUIVO";
        String local = getPathAtual(fontePasta);
        String nomeCopiado = arquivoParaCopiar.getNome();
        String destinoLog = getPathAtual(destinoPasta);
        String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome do arquivo " + nomeCopiado + " para " + destinoLog;
        diario.addDiario(logMessage);
        escreverLogLegivel(logMessage);
        salvarEstado();

        destinoPasta.addArquivo(new Arquivo(arquivoParaCopiar.getNome(),arquivoParaCopiar.getConteudo()));
        System.out.println("Arquivo '"+ arquivoParaCopiar.getNome()+"' copiado para '"+pastaDestino+"'.");

        diario.limpar();
        salvarEstado();
    }

    private String getPathAtual(Pasta pas){
        if(pas==root){
            return "/";
        }
        StringBuilder pathBuilder = new StringBuilder();
        Pasta temp = pas;
        while (temp != null && temp !=root) {
            pathBuilder.insert(0,"/"+temp.getNome());
            temp = temp.getParente();
        }
        return pathBuilder.toString();
    }

    public void criarPasta(String nome){
        if(pastaAtual.encontrarSubPasta(nome).isPresent()){
            System.out.println("Erro: Pasta '"+nome+"' já existe no local.");
            return;
        }
        if(pastaAtual.encontrarArquivo(nome).isPresent()){
            System.out.println("Erro: Já existe um arquivo com o nome '"+nome+"'. Escolha outro nome para a pasta.");
            return;
        }

        String operacao = "CRIAR_PASTA";
        String local = getPathAtual();
        String nomeCriado = nome;
        String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome da pasta " + nomeCriado;
        diario.addDiario(logMessage);
        escreverLogLegivel(logMessage);
        salvarEstado();
        
        pastaAtual.addSubPasta(new Pasta(nome));
        System.out.println("Pasta '"+nome+"' criada.");

        diario.limpar();
        salvarEstado();
    }

    private void removerPastaRecursivamente(Pasta pasta) {
        List<Arquivo> arquivosParaRemover = new ArrayList<>(pasta.getArquivos());
        for (Arquivo arquivo : arquivosParaRemover) {
            pasta.removerArquivo(arquivo);
            System.out.println("  Apagando arquivo: " + arquivo.getNome());
            String operacao = "DELETAR_ARQUIVO_RECURSIVO";
            String local = getPathAtual(pasta);
            String nomeExcluido = arquivo.getNome();
            String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome do arquivo " + nomeExcluido;
            escreverLogLegivel(logMessage);
        }

        List<Pasta> subPastasParaRemover = new ArrayList<>(pasta.getSubPastas());
        for (Pasta subPasta : subPastasParaRemover) {
            removerPastaRecursivamente(subPasta);
            pasta.removerSubPasta(subPasta);
            System.out.println("  Apagando subpasta: " + subPasta.getNome());
            String operacao = "DELETAR_PASTA_RECURSIVO";
            String local = getPathAtual(pasta);
            String nomeExcluido = subPasta.getNome();
            String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome da pasta " + nomeExcluido;
            escreverLogLegivel(logMessage);
        }
    }

    public void deletarPasta(String nome){
        Optional<Pasta> pastaParaDeletarOptional = pastaAtual.encontrarSubPasta(nome);
        if(!pastaParaDeletarOptional.isPresent()){
            System.out.println("Erro: Pasta '"+nome+"' não encontrada.");
            return;
        }
        Pasta pastaParaDeletar = pastaParaDeletarOptional.get();

        String operacao = "DELETAR_PASTA";
        String local = getPathAtual();
        String nomeExcluido = nome;
        String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome da pasta " + nomeExcluido;
        diario.addDiario(logMessage);
        escreverLogLegivel(logMessage); 
        salvarEstado();

        removerPastaRecursivamente(pastaParaDeletar);
        
        pastaAtual.removerSubPasta(pastaParaDeletar);
        System.out.println("Pasta '"+nome+"' e seu conteúdo apagados com sucesso.");

        diario.limpar();
        salvarEstado();
    }

    public void renomearPasta(String nomeAntigo,String nomeNovo){
        Optional<Pasta> pastaParaRenomear = pastaAtual.encontrarSubPasta(nomeAntigo);
        if(!pastaParaRenomear.isPresent()){
            System.out.println("Erro: Pasta '"+nomeAntigo+"' não encontrada.");
            return;
        }
        if(pastaAtual.encontrarSubPasta(nomeNovo).isPresent() || pastaAtual.encontrarArquivo(nomeNovo).isPresent()){
            System.out.println("Erro: Já existe arquivo ou pasta com o nome '"+nomeNovo+"'.");
            return;
        }

        String operacao = "RENOMEAR_PASTA";
        String local = getPathAtual();
        String nomeAntigoLog = nomeAntigo;
        String nomeNovoLog = nomeNovo;
        String logMessage = "[SALVO] Operacao: " + operacao + " // local " + local + " // nome da pasta (de/para) " + nomeAntigoLog + "/" + nomeNovoLog;
        diario.addDiario(logMessage);
        escreverLogLegivel(logMessage);
        salvarEstado();

        pastaParaRenomear.get().setNome(nomeNovo);
        System.out.println("Pasta '"+nomeAntigo+"' renomeada para '"+nomeNovo+"'.");

        diario.limpar();
        salvarEstado();
    }

    public void listarConteudo(){
        System.out.println("Conteúdo de "+ getPathAtual()+":");
        if(pastaAtual.getSubPastas().isEmpty() && pastaAtual.getArquivos().isEmpty()){
            System.out.println("  (Vazio)");
            return;
        }
        pastaAtual.getSubPastas().forEach(d -> System.out.println("  [DIR] "+d.getNome()));
        pastaAtual.getArquivos().forEach(f -> System.out.println("  [ARQ] "+ f.getNome()));
    } 

    public void Comando(){
        System.out.println("Simulador de Sistema de Arquivos");
        System.out.println("Comandos disponíveis:");
        System.out.println("  cs                          - Listar comandos (este menu)");
        System.out.println("  ls                          - Listar conteúdo do diretório atual");
        System.out.println("  cd <diretorio>              - Mudar de diretório");
        System.out.println("  mkdir <nome_diretorio>      - Criar diretório");
        System.out.println("  rmdir <nome_diretorio>      - Apagar diretório (inclusive se tiver conteúdo)");
        System.out.println("  mvdir <antigo_nome> <novo_nome> - Renomear diretório");
        System.out.println("  mkfile <nome_arquivo> <conteudo> - Criar arquivo");
        System.out.println("  rmfile <nome_arquivo>       - Apagar arquivo");
        System.out.println("  mvfile <antigo_nome> <novo_nome> - Renomear arquivo");
        System.out.println("  cpfile <caminho_origem> <caminho_destino_diretorio> - Copiar arquivo");
        System.out.println("  exit                        - Sair do simulador");
        System.out.println("--------------------------------------------------");
    }

    public static void main(String[] args) {
        SimuladoSistemaArquivo simulador = new SimuladoSistemaArquivo();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        simulador.Comando();
        String line;
        try{
            while (true) {
                System.out.print(simulador.getPathAtual()+"$ ");
                line = reader.readLine();
                if(line == null){
                    continue;
                }
                String[] parts = line.trim().split("\\s+",3);
                String comando = parts[0];

                switch (comando) {
                    case "cs":
                        simulador.Comando();
                        break;
                    case "ls":
                        simulador.listarConteudo();
                        break;
                    case "cd":
                        if(parts.length>1){
                            simulador.mudarPasta(parts[1]);
                        }else{
                            System.out.println("Uso: cd <diretorio>");
                        }
                        break;
                    case "mkdir":
                        if(parts.length>1){
                            simulador.criarPasta(parts[1]);
                        }else{
                            System.out.println("Uso: mkdir <nome_diretorio>");
                        }
                        break;
                    case "rmdir":
                        if(parts.length>1){
                            simulador.deletarPasta(parts[1]);
                        }else{
                            System.out.println("Uso: rmdir <nome_diretorio>");
                        }
                        break;
                    case "mvdir":
                        if(parts.length>2){
                            simulador.renomearPasta(parts[1],parts[2]);
                        }else{
                            System.out.println("Uso: mvdir <antigo_nome> <novo_nome>");
                        }
                        break;
                    case "mkfile":
                        if(parts.length>2){
                            String nomeDoArquivo = parts[1];
                            String conteudoDoArquivo = parts[2];
                            simulador.criarArquivo(nomeDoArquivo, conteudoDoArquivo);
                        }else{
                            System.out.println("Uso: mkfile <nome_arquivo> <conteudo>");
                        }
                        break;
                    case "rmfile":
                        if(parts.length>1){
                            simulador.deletarArquivo(parts[1]);
                        }else{
                            System.out.println("Uso: rmfile <nome_arquivo>");
                        }
                        break;
                    case "mvfile":
                        if (parts.length > 2) {
                            simulador.renomearArquivo(parts[1], parts[2]);
                        } else {
                            System.out.println("Uso: mvfile <antigo_nome> <novo_nome>");
                        }
                        break;
                    case "cpfile":
                        if (parts.length > 2) {
                            simulador.copiarArquivo(parts[1], parts[2]);
                        } else {
                            System.out.println("Uso: cpfile <caminho_origem> <caminho_destino_diretorio>");
                        }
                        break;
                    case "exit":
                        System.out.println("Saindo do simulador...");
                        return;
                    default:
                        System.out.println("Comando desconhecido: " + comando);
                        break;
                }
            }
        } catch(IOException e){
            System.err.println("Erro de leitura: " + e.getMessage());
        }
    }
}