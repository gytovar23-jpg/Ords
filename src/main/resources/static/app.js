const moeda = new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL'});
const peso = new Intl.NumberFormat('pt-BR', {maximumFractionDigits: 3});
const dataHora = new Intl.DateTimeFormat('pt-BR', {dateStyle: 'short', timeStyle: 'short'});

const $ = id => document.getElementById(id);

let materiais = [];
let clientes = [];
// Id da compra/venda que está sendo alterada em cada formulário; null quando o formulário é de registro novo
const emEdicao = {compra: null, venda: null};

// O servidor entrega o token CSRF em cookie e espera recebê-lo de volta neste cabeçalho
function cabecalhoCsrf() {
    const token = document.cookie.split('; ').find(c => c.startsWith('XSRF-TOKEN='));
    return token ? {'X-XSRF-TOKEN': decodeURIComponent(token.split('=')[1])} : {};
}

async function api(caminho, metodo = 'GET', corpo) {
    const resposta = await fetch('/api' + caminho, {
        method: metodo,
        headers: {...(corpo ? {'Content-Type': 'application/json'} : {}), ...(metodo === 'GET' ? {} : cabecalhoCsrf())},
        body: corpo ? JSON.stringify(corpo) : undefined
    });
    // Sessão expirada ou encerrada: volta para a tela de entrada
    if (resposta.status === 401) {
        location.replace('/login.html');
        throw new Error('Sessão expirada. Entre novamente.');
    }
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

function botaoDeLinha(texto, classe, aoClicar) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.className = classe;
    botao.textContent = texto;
    botao.onclick = aoClicar;
    return botao;
}

const botaoExcluir = aoClicar => botaoDeLinha('Excluir', 'remover', aoClicar);

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

// valores (opcional): item já gravado, para preencher a linha ao alterar uma compra ou venda
function adicionarItem(tipo, valores) {
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
    if (valores) {
        material.value = valores.materialId;
        pesoKg.value = valores.pesoKg;
        precoKg.value = valores.precoKg;
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

// Botões "Alterar" e "Excluir" da linha de uma compra ou venda
function acoesDoRegistro(tipo, registro, pergunta) {
    const acoes = document.createElement('div');
    acoes.className = 'grupo';
    acoes.append(
        botaoDeLinha('Alterar', 'editar', () => editar(tipo, registro)),
        botaoExcluir(() => excluir(`/${tipo}s/${registro.id}`, pergunta)));
    return acoes;
}

async function carregarCompras() {
    const compras = await api('/compras');
    preencher($('lista-compras'), compras.map(c => [
        dataHora.format(new Date(c.data)),
        c.clienteNome,
        descreverItens(c.itens),
        num(moeda.format(c.valorTotal)),
        acoesDoRegistro('compra', c, `Excluir a compra de ${moeda.format(c.valorTotal)}? O material sai do estoque.`)
    ]), 5);
    encerrarEdicaoSeExcluido('compra', compras);
}

async function carregarVendas() {
    const vendas = await api('/vendas');
    preencher($('lista-vendas'), vendas.map(v => [
        dataHora.format(new Date(v.data)),
        v.comprador,
        descreverItens(v.itens),
        num(moeda.format(v.valorTotal)),
        acoesDoRegistro('venda', v, `Excluir a venda de ${moeda.format(v.valorTotal)}? O material volta para o estoque.`)
    ]), 5);
    encerrarEdicaoSeExcluido('venda', vendas);
}

/* ---------- Alteração de compra e de venda ---------- */

// Leva a compra/venda para o formulário do topo, que passa a salvar por cima dela
function editar(tipo, registro) {
    emEdicao[tipo] = registro.id;
    if (tipo === 'compra') $('compra-cliente').value = registro.clienteId;
    else $('venda-comprador').value = registro.comprador ?? '';
    $(tipo + '-itens').replaceChildren();
    for (const item of registro.itens) adicionarItem(tipo, item);

    $(tipo + '-titulo').textContent = `Alterar ${tipo} de ${dataHora.format(new Date(registro.data))}`;
    $(tipo + '-enviar').textContent = 'Salvar alteração';
    $(tipo + '-cancelar').hidden = false;
    $('form-' + tipo).classList.add('editando');
    $(tipo + '-titulo').scrollIntoView({behavior: 'smooth', block: 'start'});
}

function encerrarEdicao(tipo) {
    emEdicao[tipo] = null;
    $('form-' + tipo).reset();
    limparItens(tipo);

    $(tipo + '-titulo').textContent = 'Nova ' + tipo;
    $(tipo + '-enviar').textContent = 'Registrar ' + tipo;
    $(tipo + '-cancelar').hidden = true;
    $('form-' + tipo).classList.remove('editando');
}

// O registro que estava sendo alterado foi excluído: o formulário volta a ser de registro novo
function encerrarEdicaoSeExcluido(tipo, registros) {
    if (emEdicao[tipo] !== null && !registros.some(r => r.id === emEdicao[tipo])) encerrarEdicao(tipo);
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

    preencher($('lista-meses'), [...f.meses].reverse().map(m => {
        const resultado = m.vendas - m.compras;
        return [
            new Date(m.mes + '-01T00:00:00').toLocaleDateString('pt-BR', {month: 'long', year: 'numeric'}),
            num(moeda.format(m.compras)),
            num(moeda.format(m.vendas)),
            {texto: moeda.format(resultado), classe: 'num ' + classe(resultado)}
        ];
    }), 4);

    desenharGraficos(f);
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

// Registra um novo ou salva por cima do que está em alteração
async function salvar(tipo, dados) {
    const alterando = emEdicao[tipo] !== null;
    const registro = alterando
        ? await api(`/${tipo}s/${emEdicao[tipo]}`, 'PUT', dados)
        : await api(`/${tipo}s`, 'POST', dados);
    encerrarEdicao(tipo);
    const nome = tipo === 'compra' ? 'Compra' : 'Venda';
    avisar(`${nome} ${alterando ? 'alterada' : 'registrada'}: ${moeda.format(registro.valorTotal)}. Estoque atualizado.`);
}

aoEnviar('form-compra', () => salvar('compra', {clienteId: Number($('compra-cliente').value), itens: lerItens('compra')}));
aoEnviar('form-venda', () => salvar('venda', {comprador: $('venda-comprador').value, itens: lerItens('venda')}));

for (const tipo of ['compra', 'venda']) $(tipo + '-cancelar').onclick = () => encerrarEdicao(tipo);

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
        desenharGraficos();
    };
}

$('sair').onclick = async () => {
    try {
        await api('/auth/logout', 'POST');
        location.replace('/login.html');
    } catch (e) {
        avisar(e.message, true);
    }
};

adicionarItem('compra');
adicionarItem('venda');
carregarTudo().catch(e => avisar(e.message, true));
