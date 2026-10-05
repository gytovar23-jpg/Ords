-- O preço passou a ser informado em cada compra/venda. A coluna antiga ficou obrigatória no banco
-- e impedia o cadastro de novos materiais; o ddl-auto=update não remove colunas sozinho.
alter table if exists materiais drop column if exists preco_por_kg;
