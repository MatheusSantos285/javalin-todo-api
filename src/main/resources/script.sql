DROP TABLE IF EXISTS tarefas;

CREATE TABLE tarefas (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    concluida BOOLEAN NOT NULL DEFAULT FALSE,
    dataCriacao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tarefas (titulo, descricao) VALUES
 ('Fazer AT de Desenvolvimento de Software', 'Estruturar o projeto com Javalin, JDBI e H2 Database.'),
 ('Estudar para a prova', 'Revisar o conteúdo de concorrência e paralelismo.'),
 ('Comprar café', 'Tipo arábica, moído na hora.'),
 ('Pagar conta de luz', NULL), -- Exemplo de tarefa sem descrição
 ('Levar o lixo para fora', 'Fazer isso antes das 19h.'),
 ('Preparar apresentação', 'Incluir os gráficos de vendas do último trimestre.'),
 ('Fazer matrícula na academia', NULL);