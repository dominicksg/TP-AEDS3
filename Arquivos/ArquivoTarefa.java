package Arquivos;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import Objetos.ParIdId;
import Objetos.ParNomeId;
import Objetos.Tarefas;

public class ArquivoTarefa extends Arquivos.Arquivo<Tarefas> {

    Arquivo<Tarefas> arqTarefas;
    HashExtensivel<ParNomeId> indiceIndiretoParNomeIdTarefas;
    ArvoreBMais<ParIdId> arvore;
    ListaInvertida listaInvertida;
    Set<String> stopWords;

    public ArquivoTarefa() throws Exception {
        super("tarefas", Tarefas.class.getConstructor());

        arvore = new ArvoreBMais<>(
            ParIdId.class.getConstructor(), 5, 
            "dados/arvore.db");

        indiceIndiretoParNomeIdTarefas = new HashExtensivel<>(
            ParNomeId.class.getConstructor(), 
            4, 
            ".\\dados\\indiceNomeTarefas.hash_d.db", 
            ".\\dados\\indiceNomeTarefas.hash_c.db"
        );

        listaInvertida = new ListaInvertida(4, "dados/dicionario.listainv.db", "dados/blocos.listainv.db");

        stopWords = new HashSet<>(Files.readAllLines(Paths.get("dados/stopwords.txt")));
    }

    @Override
    public int create(Tarefas c) throws Exception {
        int id = super.create(c);
        indiceIndiretoParNomeIdTarefas.create(new ParNomeId(c.getNome(), id));
        if(arvore.create(new ParIdId(c.getIdCategoria(), c.getId()))){
            System.out.println("Item inserido!");
        }
        //chamar aqui a função de insertLista passando como paramentro o id da tarefa e o c.splitDescrição

        return id;
    }

    public Tarefas read(String nome) throws Exception {
        //System.out.println(ParNomeId.hash(nome));
        ParNomeId pni = indiceIndiretoParNomeIdTarefas.read(ParNomeId.hash(nome));
        if(pni == null){
            //System.out.println("entrei");
            return null;
        }
            
        return read(pni.getId());
    }
    
    public boolean delete(String nome) throws Exception {
        ParNomeId pni = indiceIndiretoParNomeIdTarefas.read(ParNomeId.hash(nome));
        Tarefas tarefa = super.read(pni.getId());

        ParIdId pii = new ParIdId(tarefa.getIdCategoria(), tarefa.getId());
        arvore.delete(pii);

        if(delete(pni.getId()))
            return indiceIndiretoParNomeIdTarefas.delete(ParNomeId.hash(nome));

        return false;
    }

    //@Override
    public boolean update(Tarefas novaTarefa, String nome) throws Exception {
        Tarefas tarefaVelha = read(nome);
        if(super.update(novaTarefa)) {
            if(novaTarefa.getNome().compareTo(tarefaVelha.getNome())!=0) {
                indiceIndiretoParNomeIdTarefas.delete(ParNomeId.hash(tarefaVelha.getNome()));
                indiceIndiretoParNomeIdTarefas.create(new ParNomeId(novaTarefa.getNome(), novaTarefa.getId()));
            }
            return true;
        }
        return false;
    }

    public boolean update(Tarefas novaTarefa, String nome, int idVelho) throws Exception {
        Tarefas tarefaVelha = read(idVelho);
        if(super.update(novaTarefa)) {
                indiceIndiretoParNomeIdTarefas.delete(ParNomeId.hash(tarefaVelha.getNome()));
                indiceIndiretoParNomeIdTarefas.create(new ParNomeId(novaTarefa.getNome(), novaTarefa.getId()));
                ParIdId piiVelho = new ParIdId(tarefaVelha.getIdCategoria(), tarefaVelha.getId());
                ParIdId pii = new ParIdId(novaTarefa.getIdCategoria(), novaTarefa.getId());
                arvore.delete(piiVelho);
                arvore.create(pii);

            return true;
        }
        return false;
    }

    

    public ArrayList<ParIdId> getAllStacksFromCategorie(int id1)throws Exception{
        ArrayList<ParIdId> lista = new ArrayList<>();
        ParIdId pii = new ParIdId(id1, -1);
        lista = arvore.read(pii);
        
        return lista;
    }

    //Função ainda n finalizada
    private boolean insertList (String[] listaChaves, int id){
        int qtdPalavras = listaChaves.length;
        for(int i = 0; i < qtdPalavras; i++){
            if(!stopWords.contains(listaChaves[i])){
                //fazer calculo do IDF e depois chamar o create da lista invertida passando o chave, a frequencia e o id da tarefa
            }
        }
    }
}

