const moeda = new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL'});
const peso = new Intl.NumberFormat('pt-BR', {maximumFractionDigits: 3});
const dataHora = new Intl.DateTimeFormat('pt-BR', {dateStyle: 'short', timeStyle: 'short'});

const $ = id => document.getElementById(id);

let materiais = [];
let clientes = [];

async function api(caminho, metodo = 'GET', corpo) {
    const resposta = await fetch('/api' + caminho, {
        method: metodo,
        headers: corpo ? {'Content-Type': 'application/json'} : undefined,
        body: corpo ? JSON.stringify(corpo) : undefined
    });
    if (!resposta.ok) {
        const erro = await resposta.json().catch(() => ({}));
        throw new Error(erro.message || 'Erro ao comunicar com o servidor (' + resposta.status + ')');
    }
    return resposta.status === 204 ? null : resposta.json();
}

function avisar(texto, erro = false) {
    const caixa = $('mensagem');
    caixa.textContent = texto;
    caixa.classList.toggle('erro', erro);
    caixa.hidden = false;
    clearTimeout(avisar.temporizador);
    avisar.temporizador = setTimeout(() => caixa.hidden = true, 6000);
}

// Monta as linhas de uma tabela; os valores entram como texto, nunca como HTML
function preencher(tbody, linhas, colunas) {
    tbody.replaceChildren();
    if (linhas.length === 0) {
        const td = document.createElement('td');
        td.colSpan = colunas;
        td.className = 'vazio';
        td.textContent = 'Nenhum registro ainda.';
        tbody.append(document.createElement('tr'));
        tbody.lastChild.append(td);
        return;
    }
    for (const celulas of linhas) {
        const tr = document.createElement('tr');
        for (const celula of celulas) {
            const td = document.createElement('td');
            if (celula instanceof Node) {
                td.append(celula);
            } else if (celula && typeof celula === 'object') {
                td.textContent = celula.texto;
                td.className = celula.classe;
            } else {
                td.textContent = celula ?? '—';
            }
            tr.append(td);
        }
        tbody.append(tr);
    }
}

const num = texto => ({texto, classe: 'num'});

function botaoExcluir(aoClicar) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.className = 'remover';
    botao.textContent = 'Excluir';
    botao.onclick = aoClicar;
    return botao;
}

function descreverItens(itens) {
    const lista = document.createElement('div');
    for (const item of itens) {
        const linha = document.createElement('div');
        linha.textContent = `${item.materialNome}: ${peso.format(item.pesoKg)} kg × ${moeda.format(item.precoKg)}`;
        lista.append(linha);
    }
    return lista;
}

/* ---------- Itens de compra e de venda ---------- */

function adicionarItem(tipo) {
    const tr = document.createElement('tr');

    const material = document.createElement('select');
    material.required = true;
    material.dataset.campo = 'material';
    preencherMateriais(material, tipo);

    const pesoKg = document.createElement('input');
    Object.assign(pesoKg, {type: 'number', min: '0.001', step: '0.001', required: true});
    pesoKg.dataset.campo = 'peso';

    const precoKg = document.createElement('input');
    Object.assign(precoKg, {type: 'number', min: '0', step: '0.01', required: true});
    precoKg.dataset.campo = 'preco';

    const total = document.createElement('span');
    total.dataset.campo = 'total';

    const remover = document.createElement('button');
    remover.type = 'button';
    remover.className = 'remover';
    remover.textContent = '✕';
    remover.title = 'Remover item';
    remover.onclick = () => {
        tr.remove();
        if ($(tipo + '-itens').children.length === 0) adicionarItem(tipo);
        atualizarTotal(tipo);
    };

    for (const [elemento, classe] of [[material], [pesoKg], [precoKg], [total, 'num'], [remover]]) {
        const td = document.createElement('td');
        if (classe) td.className = classe;
        td.append(elemento);
        tr.append(td);
    }
    tr.oninput = () => atualizarTotal(tipo);
    $(tipo + '-itens').append(tr);
    atualizarTotal(tipo);
}

function preencherMateriais(select, tipo) {
    const selecionado = select.value;
    select.replaceChildren(new Option('Selecione…', ''));
    for (const m of materiais) {
        // Na venda mostramos o saldo, que é o limite do que pode sair
        const rotulo = tipo === 'venda' ? `${m.nome} (${peso.format(m.estoqueKg)} kg)` : m.nome;
        select.add(new Option(rotulo, m.id));
    }
    select.value = selecionado;
}

function lerItens(tipo) {
    return [...$(tipo + '-itens').children].map(tr => ({
        materialId: Number(tr.querySelector('[data-campo=material]').value),
        pesoKg: Number(tr.querySelector('[data-campo=peso]').value),
        precoKg: Number(tr.querySelector('[data-campo=preco]').value)
    }));
}

function atualizarTotal(tipo) {
    let total = 0;
    for (const tr of $(tipo + '-itens').children) {
        const valor = Number(tr.querySelector('[data-campo=peso]').value) * Number(tr.querySelector('[data-campo=preco]').value);
        tr.querySelector('[data-campo=total]').textContent = moeda.format(valor || 0);
        total += valor || 0;
    }
    $(tipo + '-total').textContent = moeda.format(total);
}

function limparItens(tipo) {
    $(tipo + '-itens').replaceChildren();
    adicionarItem(tipo);
}

/* ---------- Carregamento das telas ---------- */

async function carregarMateriais() {
    materiais = await api('/materiais');

    for (const tipo of ['compra', 'venda']) {
        for (const select of $(tipo + '-itens').querySelectorAll('select')) preencherMateriais(select, tipo);
    }

    const total = materiais.reduce((soma, m) => soma + Number(m.estoqueKg), 0);
    $('estoque-total').textContent = peso.format(total) + ' kg';

    preencher($('lista-estoque'), materiais.map(m => [
        m.nome,
        m.categoria,
        num(peso.format(m.estoqueKg) + ' kg'),
        botaoExcluir(() => excluir('/materiais/' + m.id, `Excluir o material "${m.nome}"?`))
    ]), 4);
}

async function carregarClientes() {
    clientes = await api('/clientes');
    clientes.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));

    const select = $('compra-cliente');
    const selecionado = select.value;
    select.replaceChildren(new Option('Selecione…', ''));
    for (const c of clientes) select.add(new Option(c.documento ? `${c.nome} — ${c.documento}` : c.nome, c.id));
    select.value = selecionado;

    preencher($('lista-clientes'), clientes.map(c => [
        c.nome,
        c.documento,
        c.telefone,
        botaoExcluir(() => excluir('/clientes/' + c.id, `Excluir o cliente "${c.nome}"?`))
    ]), 4);
}

async function carregarCompras() {
    const compras = await api('/compras');
    preencher($('lista-compras'), compras.map(c => [
        dataHora.format(new Date(c.data)),
        c.clienteNome,
        descreverItens(c.itens),
        num(moeda.format(c.valorTotal))
    ]), 4);
}

async function carregarVendas() {
    const vendas = await api('/vendas');
    preencher($('lista-vendas'), vendas.map(v => [
        dataHora.format(new Date(v.data)),
        v.comprador,
        descreverItens(v.itens),
        num(moeda.format(v.valorTotal))
    ]), 4);
}

async function carregarFinanceiro() {
    const f = await api('/financeiro');
    const classe = valor => valor > 0 ? 'lucro' : valor < 0 ? 'prejuizo' : '';

    $('fin-compras').textContent = moeda.format(f.totalCompras);
    $('fin-vendas').textContent = moeda.format(f.totalVendas);
    $('fin-resultado').textContent = moeda.format(f.resultado);
    $('fin-resultado').className = classe(f.resultado);
    $('fin-rotulo').textContent = f.resultado > 0 ? 'Lucro' : f.resultado < 0 ? 'Prejuízo' : 'Resultado';

    preencher($('lista-financeiro'), f.materiais.map(m => {
        const resultado = m.valorVendido - m.valorComprado;
        return [
            m.materialNome,
            num(peso.format(m.kgComprado)),
            num(moeda.format(m.valorComprado)),
            num(peso.format(m.kgVendido)),
            num(moeda.format(m.valorVendido)),
            num(peso.format(m.estoqueKg)),
            {texto: moeda.format(resultado), classe: 'num ' + classe(resultado)}
        ];
    }), 7);
}

async function carregarTudo() {
    await Promise.all([carregarMateriais(), carregarClientes(), carregarCompras(), carregarVendas(), carregarFinanceiro()]);
}

/* ---------- Ações ---------- */

async function excluir(caminho, pergunta) {
    if (!confirm(pergunta)) return;
    try {
        await api(caminho, 'DELETE');
        await carregarTudo();
    } catch (e) {
        avisar(e.message, true);
    }
}

function aoEnviar(idFormulario, acao) {
    $(idFormulario).addEventListener('submit', async evento => {
        evento.preventDefault();
        const botao = evento.target.querySelector('[type=submit]');
        botao.disabled = true;
        try {
            await acao();
            await carregarTudo();
        } catch (e) {
            avisar(e.message, true);
        } finally {
            botao.disabled = false;
        }
    });
}

aoEnviar('form-compra', async () => {
    const compra = await api('/compras', 'POST', {clienteId: Number($('compra-cliente').value), itens: lerItens('compra')});
    $('form-compra').reset();
    limparItens('compra');
    avisar(`Compra registrada: ${moeda.format(compra.valorTotal)}. Estoque atualizado.`);
});

aoEnviar('form-venda', async () => {
    const venda = await api('/vendas', 'POST', {comprador: $('venda-comprador').value, itens: lerItens('venda')});
    $('form-venda').reset();
    limparItens('venda');
    avisar(`Venda registrada: ${moeda.format(venda.valorTotal)}. Estoque atualizado.`);
});

aoEnviar('form-material', async () => {
    await api('/materiais', 'POST', {nome: $('material-nome').value, categoria: $('material-categoria').value});
    $('form-material').reset();
    avisar('Material cadastrado.');
});

aoEnviar('form-cliente', async () => {
    await api('/clientes', 'POST', {
        nome: $('cliente-nome').value,
        documento: $('cliente-documento').value,
        telefone: $('cliente-telefone').value
    });
    $('form-cliente').reset();
    avisar('Cliente cadastrado.');
});

for (const botao of document.querySelectorAll('[data-adicionar]')) {
    botao.onclick = () => adicionarItem(botao.dataset.adicionar);
}

for (const aba of $('abas').children) {
    aba.onclick = () => {
        for (const outra of $('abas').children) outra.classList.toggle('ativa', outra === aba);
        for (const secao of document.querySelectorAll('main > section')) secao.hidden = secao.id !== aba.dataset.aba;
    };
}

adicionarItem('compra');
adicionarItem('venda');
carregarTudo().catch(e => avisar(e.message, true));
