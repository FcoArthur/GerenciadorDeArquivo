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
import java.util.Arrays;
import java.util.Optional;
import java.time.LocalDateTime; // Import para o log TXT
import java.time.format.DateTimeFormatter; // Import para o log TXT


public class SimuladoSistemaArquivo implements Serializable {
    private static final long serialVersionUID = 1L; // Corrigido de serialUID para serialVersionUID
    private Pasta root;
    private Pasta pastaAtual;
    private Diario diario;
    private static final String DIARIO_ARQUIVO = "arquivodosistema_diario.ser";
    private static final String ESTADODOARQUIVODOSISTEMA_ARQUIVO = "arquivodosistema_estado.ser";
    private static final String LOG_JORNAL_LEGIVEL = "log_jornal.txt"; // NOVO: Arquivo de log TXT

    public SimuladoSistemaArquivo() {
        this.root = new Pasta("root");
        this.pastaAtual = root;
        this.diario = new Diario();
        carregarEstado();
        recuperarDoDiario(); // **IMPORTANTE: DESCOMENTADO PARA O JOURNALING FUNCIONAR**
    }

    // NOVO: Método para escrever no log TXT
    private void escreverLogLegivel(String mensagem) {
        try (FileWriter fw = new FileWriter(LOG_JORNAL_LEGIVEL, true); // 'true' para adicionar ao final
             PrintWriter pw = new PrintWriter(fw)) {
            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            pw.println(agora.format(formatador) + " - " + mensagem);
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log TXT: " + e.getMessage());
        }
    }

    //salvar e carregar
    private void salvarEstado(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ESTADODOARQUIVODOSISTEMA_ARQUIVO))){
            oos.writeObject(this.root);
            // System.out.println("estado do sis,arq salvo"); // Comentado para evitar poluir o console
        }catch(IOException e){
            System.err.println("Erro ao salvar estado do sistema de arquivos: "+ e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DIARIO_ARQUIVO))) {
            oos.writeObject(this.diario);
            // System.out.println("diario salvo"); // Comentado para evitar poluir o console
        } catch(IOException e){
            System.err.println("Erro ao salvar diário: "+e.getMessage());
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
        File arquivoDoDiario = new File(DIARIO_ARQUIVO);
        if(arquivoDoDiario.exists()){
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivoDoDiario))){
                this.diario = (Diario) ois.readObject();
                System.out.println("Diário de operações carregado.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao carregar diário: "+ e.getMessage());
                this.diario = new Diario();
            }
        }else {
            System.out.println("Nenhum diário existente. Criando um novo.");
        }
    }

    private void recuperarDoDiario(){
        if(!diario.getDiarios().isEmpty()){
            System.out.println("Iniciando recuperação do diário...");
            escreverLogLegivel("--- INICIANDO RECUPERAÇÃO DO DIÁRIO ---");
            for (EntradaNoDiario entradaDiario : diario.getDiarios()) { // Renomeado 'diario' para 'entradaDiario' para evitar conflito
                System.out.println("Recuperando: " + entradaDiario);
                escreverLogLegivel("[RECUPERANDO] " + entradaDiario.toString());
                switch (entradaDiario.getTipo()) {
                    case CRIAR_ARQUIVO:
                        // No caso de CRIAR_ARQUIVO, caminho é o diretório pai, novoCaminho é o nome do arquivo, conteudo é o conteúdo
                        Pasta pastaCriarArquivo = getPastaPorCaminho(entradaDiario.getCaminho()).orElse(null);
                        if(pastaCriarArquivo != null){
                            if(!pastaCriarArquivo.encontrarArquivo(entradaDiario.getNovoCaminho()).isPresent()){
                                pastaCriarArquivo.addArquivo(new Arquivo(entradaDiario.getNovoCaminho(), entradaDiario.getConteudo()));
                                System.out.println("  Recuperado: Arquivo '"+ entradaDiario.getNovoCaminho() + "' criado em '" + entradaDiario.getCaminho() + "'");
                            }
                        }
                        break;
                    case DELETAR_ARQUIVO:
                        // caminho é o diretório pai, novoCaminho é o nome do arquivo a ser deletado
                        Pasta pastaDeletarArquivo = getPastaPorCaminho(entradaDiario.getCaminho()).orElse(null);
                        if(pastaDeletarArquivo!= null){
                            pastaDeletarArquivo.encontrarArquivo(entradaDiario.getNovoCaminho()).ifPresent(f ->{
                                pastaDeletarArquivo.removerArquivo(f);
                                System.out.println("  Recuperado: Arquivo '"+ entradaDiario.getNovoCaminho() + "' excluído de '"+ entradaDiario.getCaminho() + "'");
                            });
                        }
                        break;
                    case COPIAR_ARQUIVO:
                        // caminho é o diretório de origem, novoCaminho é o nome do arquivo de origem, conteudo é o diretório de destino
                        Pasta pastaCopiarOrigemArquivo = getPastaPorCaminho(entradaDiario.getCaminho()).orElse(null); // Corrigido
                        Pasta pastaCopiarDestinoArquivo = getPastaPorCaminho(entradaDiario.getConteudo()).orElse(null); // Corrigido

                        if(pastaCopiarOrigemArquivo!= null && pastaCopiarDestinoArquivo!=null){
                            pastaCopiarOrigemArquivo.encontrarArquivo(entradaDiario.getNovoCaminho()).ifPresent(arquivoParaCopiar ->{
                                if(!pastaCopiarDestinoArquivo.encontrarArquivo(arquivoParaCopiar.getNome()).isPresent()){
                                    pastaCopiarDestinoArquivo.addArquivo(new Arquivo(arquivoParaCopiar.getNome(),arquivoParaCopiar.getConteudo()));
                                    System.out.println("  Recuperado: Arquivo '"+arquivoParaCopiar.getNome()+ "' copiado de '"+entradaDiario.getCaminho()+"' para '"+entradaDiario.getConteudo()+"'");
                                }
                            });
                        }
                        break;
                    case RENOMEAR_ARQUIVO:
                        // caminho é o diretório pai, novoCaminho é o nome antigo, conteudo é o nome novo
                        Pasta pastaRenomearArquivo = getPastaPorCaminho(entradaDiario.getCaminho()).orElse(null);
                        if(pastaRenomearArquivo!=null){
                            pastaRenomearArquivo.encontrarArquivo(entradaDiario.getNovoCaminho()).ifPresent(f->{
                                f.setNome(entradaDiario.getConteudo());
                                System.out.println("  Recuperado: Arquivo '"+entradaDiario.getNovoCaminho() + "' renomeado para '"+ entradaDiario.getConteudo()+ "' em '"+entradaDiario.getCaminho()+"'");
                            });
                        }
                        break;
                    case CRIAR_PASTA:
                        // caminho é o diretório pai, novoCaminho é o nome da nova pasta
                        Pasta pastaCriarPasta = getPastaPorCaminho(entradaDiario.getCaminho()).orElse(null);
                        if(pastaCriarPasta!=null){
                            if(!pastaCriarPasta.encontrarSubPasta(entradaDiario.getNovoCaminho()).isPresent()){
                                pastaCriarPasta.addSubPasta(new Pasta(entradaDiario.getNovoCaminho()));
                                System.out.println("  Recuperado: Diretório '"+entradaDiario.getNovoCaminho()+"' criado em '"+entradaDiario.getCaminho()+"'");
                            }
                        }
                        break;
                    case DELETAR_PASTA:
                        // caminho é o diretório pai, novoCaminho é o nome da pasta a ser deletada
                        Pasta pastaDeletarPasta = getPastaPorCaminho(entradaDiario.getCaminho()).orElse(null);
                        if(pastaDeletarPasta!=null){
                            pastaDeletarPasta.encontrarSubPasta(entradaDiario.getNovoCaminho()).ifPresent(d->{
                                pastaDeletarPasta.removerSubPasta(d);
                                System.out.println("  Recuperado: Diretório '"+ entradaDiario.getNovoCaminho()+"' excluído de '"+ entradaDiario.getCaminho()+"'");
                            });
                        }
                        break;
                    case RENOMEAR_PASTA:
                        // caminho é o diretório pai, novoCaminho é o nome antigo, conteudo é o nome novo
                        Pasta pastaRenomearPasta = getPastaPorCaminho(entradaDiario.getCaminho()).orElse(null);
                        if(pastaRenomearPasta!=null){
                            pastaRenomearPasta.encontrarSubPasta(entradaDiario.getNovoCaminho()).ifPresent(d->{
                                d.setNome(entradaDiario.getConteudo());
                                System.out.println("  Recuperado: Diretório '"+entradaDiario.getNovoCaminho() + "' renomeado para '"+ entradaDiario.getConteudo()+ "' em '"+entradaDiario.getCaminho()+"'");
                            });
                        }
                        break;
                }
            }
            diario.limpar();
            salvarEstado();
            System.out.println("Recuperação do diário concluída.");
            escreverLogLegivel("--- RECUPERAÇÃO DO DIÁRIO CONCLUÍDA. Diário limpo. ---");
        }else{
            System.out.println("Diário vazio. Nenhuma recuperação necessária.");
            escreverLogLegivel("Diário vazio na inicialização. Nenhuma recuperação necessária.");
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

        EntradaNoDiario novaEntrada = new EntradaNoDiario(EntradaNoDiario.TipoDeOperacao.CRIAR_ARQUIVO, getPathAtual(),nome,conteudo);
        diario.addDiario(novaEntrada);
        salvarEstado();
        escreverLogLegivel("[INICIADO] " + novaEntrada.toString());

        pastaAtual.addArquivo(new Arquivo(nome, conteudo));
        System.out.println("Arquivo '"+nome+"' criado com sucesso.");

        diario.limpar();
        salvarEstado();
        escreverLogLegivel("[CONCLUÍDO] Operação 'CRIAR_ARQUIVO' para '"+nome+"' concluída. Diário limpo.");
    }

    public void deletarArquivo(String nome){
        Optional<Arquivo> arquivoParaDeletar = pastaAtual.encontrarArquivo(nome);
        if(!arquivoParaDeletar.isPresent()){
            System.out.println("Erro: Arquivo '"+nome+"' não localizado.");
            return;
        }
        EntradaNoDiario novaEntrada = new EntradaNoDiario(EntradaNoDiario.TipoDeOperacao.DELETAR_ARQUIVO,getPathAtual(),nome);
        diario.addDiario(novaEntrada);
        salvarEstado();
        escreverLogLegivel("[INICIADO] " + novaEntrada.toString());

        pastaAtual.removerArquivo(arquivoParaDeletar.get());
        System.out.println("Arquivo '"+nome+"' apagado com sucesso.");

        diario.limpar();
        salvarEstado();
        escreverLogLegivel("[CONCLUÍDO] Operação 'DELETAR_ARQUIVO' para '"+nome+"' concluída. Diário limpo.");
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
        EntradaNoDiario novaEntrada = new EntradaNoDiario(EntradaNoDiario.TipoDeOperacao.RENOMEAR_ARQUIVO,getPathAtual(),nomeVelho,nomeNovo);
        diario.addDiario(novaEntrada);
        salvarEstado();
        escreverLogLegivel("[INICIADO] " + novaEntrada.toString());

        arquivoParaRenomear.get().setNome(nomeNovo);
        System.out.println("Arquivo '"+nomeVelho+"' renomeado para '"+nomeNovo+"'.");
        diario.limpar();
        salvarEstado();
        escreverLogLegivel("[CONCLUÍDO] Operação 'RENOMEAR_ARQUIVO' para '"+nomeVelho+"' -> '"+nomeNovo+"' concluída. Diário limpo.");
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
            System.out.println("Erro: Diretório de origem '"+ caminhoPastaFonte +"' não existe."); // Corrigido a mensagem
            return;
        }

        Optional<Arquivo> arquivoParaCopiarOptional = fontePasta.encontrarArquivo(nomeDoArquivoFonte);
        if(!arquivoParaCopiarOptional.isPresent()){
            System.out.println("Erro: Arquivo de origem '"+nomeDoArquivoFonte+"' não encontrado em '"+arquivoFonteCaminho+"'."); // Corrigido a mensagem
            return;
        }
        Arquivo arquivoParaCopiar = arquivoParaCopiarOptional.get();

        Pasta destinoPasta = getPastaPorCaminho(pastaDestino).orElse(null);
        if (destinoPasta==null) {
            System.out.println("Erro: Diretório de destino '"+ pastaDestino +"' não encontrado."); // Corrigido a mensagem
            return;
        }
        if (destinoPasta.encontrarArquivo(arquivoParaCopiar.getNome()).isPresent()){
            System.out.println("Erro: Já existe um arquivo com o nome '"+arquivoParaCopiar.getNome()+"' no destino."); // Corrigido a mensagem
            return;
        }

        EntradaNoDiario novaEntrada = new EntradaNoDiario(EntradaNoDiario.TipoDeOperacao.COPIAR_ARQUIVO, getPathAtual(fontePasta),arquivoParaCopiar.getNome(),getPathAtual(destinoPasta));
        diario.addDiario(novaEntrada);
        salvarEstado();
        escreverLogLegivel("[INICIADO] " + novaEntrada.toString());

        destinoPasta.addArquivo(new Arquivo(arquivoParaCopiar.getNome(),arquivoParaCopiar.getConteudo()));
        System.out.println("Arquivo '"+ arquivoParaCopiar.getNome()+"' copiado para '"+pastaDestino+"'.");

        diario.limpar();
        salvarEstado();
        escreverLogLegivel("[CONCLUÍDO] Operação 'COPIAR_ARQUIVO' para '"+arquivoParaCopiar.getNome()+"' concluída. Diário limpo.");
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

        EntradaNoDiario novaEntrada = new EntradaNoDiario(EntradaNoDiario.TipoDeOperacao.CRIAR_PASTA,getPathAtual(), nome);
        diario.addDiario(novaEntrada);
        salvarEstado();
        escreverLogLegivel("[INICIADO] " + novaEntrada.toString());
        
        pastaAtual.addSubPasta(new Pasta(nome));
        System.out.println("Pasta '"+nome+"' criada.");

        diario.limpar();
        salvarEstado();
        escreverLogLegivel("[CONCLUÍDO] Operação 'CRIAR_PASTA' para '"+nome+"' concluída. Diário limpo.");
    }

    public void deletarPasta(String nome){
        Optional<Pasta> pastaParaDeletar = pastaAtual.encontrarSubPasta((nome));
        if(!pastaParaDeletar.isPresent()){
            System.out.println("Erro: Pasta '"+nome+"' não encontrada.");
            return;
        }
        if(!pastaParaDeletar.get().getArquivos().isEmpty() || !pastaParaDeletar.get().getSubPastas().isEmpty()){
            System.out.println("Erro: Pasta '"+nome+"' não está vazia. Esvazie-a primeiro.");
            return;
        }

        EntradaNoDiario novaEntrada = new EntradaNoDiario(EntradaNoDiario.TipoDeOperacao.DELETAR_PASTA,getPathAtual(), nome);
        diario.addDiario(novaEntrada);
        salvarEstado();
        escreverLogLegivel("[INICIADO] " + novaEntrada.toString());

        pastaAtual.removerSubPasta(pastaParaDeletar.get());
        System.out.println("Pasta '"+nome+"' apagada.");

        diario.limpar();
        salvarEstado();
        escreverLogLegivel("[CONCLUÍDO] Operação 'DELETAR_PASTA' para '"+nome+"' concluída. Diário limpo.");
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

        EntradaNoDiario novaEntrada = new EntradaNoDiario(EntradaNoDiario.TipoDeOperacao.RENOMEAR_PASTA,getPathAtual(),nomeAntigo, nomeNovo);
        diario.addDiario(novaEntrada);
        salvarEstado();
        escreverLogLegivel("[INICIADO] " + novaEntrada.toString());

        pastaParaRenomear.get().setNome(nomeNovo);
        System.out.println("Pasta '"+nomeAntigo+"' renomeada para '"+nomeNovo+"'.");

        diario.limpar();
        salvarEstado();
        escreverLogLegivel("[CONCLUÍDO] Operação 'RENOMEAR_PASTA' para '"+nomeAntigo+"' -> '"+nomeNovo+"' concluída. Diário limpo.");
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

    public void Comando(){ // Renomeado para seguir convenção de método (primeira letra minúscula)
        System.out.println("Simulador de Sistema de Arquivos com Journaling");
        System.out.println("Comandos disponíveis:");
        System.out.println("  cs                          - Listar comandos (este menu)"); // Adicionado no código
        System.out.println("  ls                          - Listar conteúdo do diretório atual");
        System.out.println("  cd <diretorio>              - Mudar de diretório");
        System.out.println("  mkdir <nome_diretorio>      - Criar diretório");
        System.out.println("  rmdir <nome_diretorio>      - Apagar diretório (deve estar vazio)");
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
        simulador.Comando(); // Chamada para mostrar os comandos ao iniciar
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
                        simulador.Comando(); // Re-exibe os comandos
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