/* Gráficos do financeiro, desenhados em SVG (sem biblioteca externa, funcionam sem internet) */

const SVG = 'http://www.w3.org/2000/svg';
const moedaCurta = new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL', notation: 'compact', maximumFractionDigits: 1});

function svg(nome, atributos = {}) {
    const elemento = document.createElementNS(SVG, nome);
    for (const [chave, valor] of Object.entries(atributos)) elemento.setAttribute(chave, valor);
    return elemento;
}

function criar(nome, classe, texto) {
    const elemento = document.createElement(nome);
    if (classe) elemento.className = classe;
    if (texto !== undefined) elemento.textContent = texto;
    return elemento;
}

// Degraus "redondos" (1, 2, 2,5 ou 5 × 10ⁿ) para o eixo de valores
function passoRedondo(amplitude, divisoes) {
    const bruto = amplitude / divisoes;
    const potencia = 10 ** Math.floor(Math.log10(bruto));
    return [1, 2, 2.5, 5, 10].find(m => m * potencia >= bruto) * potencia;
}

// Coluna com a ponta arredondada e a base reta, encostada na linha do zero
function caminhoColuna(x, largura, yZero, yValor) {
    const altura = Math.abs(yValor - yZero);
    const r = Math.min(4, largura / 2, altura);
    const s = yValor < yZero ? -1 : 1; // para cima ou para baixo
    return `M${x},${yZero} V${yValor - s * r} Q${x},${yValor} ${x + r},${yValor} H${x + largura - r} ` +
        `Q${x + largura},${yValor} ${x + largura},${yValor - s * r} V${yZero} Z`;
}

function legenda(series) {
    const lista = criar('div', 'grafico-legenda');
    for (const serie of series) {
        const item = criar('span');
        const cor = criar('i');
        cor.style.background = serie.cor;
        item.append(cor, serie.nome);
        lista.append(item);
    }
    return lista;
}

function dica(recipiente) {
    const caixa = criar('div', 'grafico-dica');
    caixa.hidden = true;
    recipiente.append(caixa);
    return {
        mostrar(titulo, linhas, x, y) {
            caixa.replaceChildren(criar('div', 'grafico-dica-titulo', titulo));
            for (const linha of linhas) {
                const item = criar('div', 'grafico-dica-linha');
                const cor = criar('i');
                cor.style.background = linha.cor;
                item.append(cor, criar('strong', '', linha.valor), criar('span', '', linha.nome));
                caixa.append(item);
            }
            caixa.hidden = false;
            // Mantém a dica dentro do gráfico
            const maximo = recipiente.clientWidth - caixa.offsetWidth;
            caixa.style.left = Math.max(0, Math.min(x - caixa.offsetWidth / 2, maximo)) + 'px';
            caixa.style.top = Math.max(0, y - caixa.offsetHeight - 8) + 'px';
        },
        esconder() {
            caixa.hidden = true;
        }
    };
}

/*
 * Colunas agrupadas por categoria, com suporte a valores negativos.
 * series: [{nome, valores, cor}] — cor pode ser uma função do valor (ex.: lucro/prejuízo).
 */
function graficoColunas(recipiente, {categorias, rotulosLongos, series}) {
    recipiente.replaceChildren();
    if (series.length > 1) recipiente.append(legenda(series));

    // clientWidth inclui o espaçamento interno do cartão
    const estilo = getComputedStyle(recipiente);
    const largura = Math.max(recipiente.clientWidth - parseFloat(estilo.paddingLeft) - parseFloat(estilo.paddingRight), 240);
    const altura = 240;
    const margem = {cima: 12, direita: 8, baixo: 26, esquerda: 62};
    const areaL = largura - margem.esquerda - margem.direita;
    const areaA = altura - margem.cima - margem.baixo;

    const valores = series.flatMap(s => s.valores);
    const passo = passoRedondo((Math.max(0, ...valores) - Math.min(0, ...valores)) || 1, 4);
    const minimo = Math.floor(Math.min(0, ...valores) / passo) * passo;
    const maximo = Math.ceil(Math.max(0, ...valores) / passo) * passo || passo;
    const y = valor => margem.cima + areaA * (maximo - valor) / (maximo - minimo);

    const desenho = svg('svg', {width: largura, height: altura, role: 'img'});

    for (let marca = minimo; marca <= maximo + passo / 2; marca += passo) {
        desenho.append(svg('line', {
            x1: margem.esquerda, x2: largura - margem.direita, y1: y(marca), y2: y(marca),
            class: Math.abs(marca) < passo / 2 ? 'grafico-base' : 'grafico-grade'
        }));
        const rotulo = svg('text', {x: margem.esquerda - 8, y: y(marca) + 4, 'text-anchor': 'end', class: 'grafico-eixo'});
        rotulo.textContent = moedaCurta.format(marca);
        desenho.append(rotulo);
    }

    const faixa = areaL / categorias.length;
    const larguraColuna = Math.max(3, Math.min(24, (faixa * 0.7 - 2 * (series.length - 1)) / series.length));
    const larguraGrupo = larguraColuna * series.length + 2 * (series.length - 1);
    // Em telas estreitas, mostra um rótulo sim e outro não
    const saltoRotulo = Math.ceil(44 / faixa);
    const caixaDica = dica(recipiente);

    categorias.forEach((categoria, i) => {
        const inicio = margem.esquerda + faixa * i;
        const grupo = svg('g', {class: 'grafico-grupo', tabindex: 0});

        // Área de toque: a faixa inteira, não só a coluna pintada
        grupo.append(svg('rect', {x: inicio, y: margem.cima, width: faixa, height: areaA, class: 'grafico-toque'}));

        series.forEach((serie, j) => {
            const valor = serie.valores[i];
            if (!valor) return;
            const x = inicio + (faixa - larguraGrupo) / 2 + j * (larguraColuna + 2);
            const coluna = svg('path', {d: caminhoColuna(x, larguraColuna, y(0), y(valor))});
            coluna.style.fill = typeof serie.cor === 'function' ? serie.cor(valor) : serie.cor;
            grupo.append(coluna);
        });

        if ((categorias.length - 1 - i) % saltoRotulo === 0) {
            const rotulo = svg('text', {x: inicio + faixa / 2, y: altura - 8, 'text-anchor': 'middle', class: 'grafico-eixo'});
            rotulo.textContent = categoria;
            grupo.append(rotulo);
        }

        const mostrar = () => caixaDica.mostrar(
            rotulosLongos[i],
            series.map(s => ({
                nome: s.nome,
                valor: moeda.format(s.valores[i]),
                cor: typeof s.cor === 'function' ? s.cor(s.valores[i]) : s.cor
            })),
            inicio + faixa / 2,
            Math.min(...series.map(s => y(Math.max(0, s.valores[i])))));
        grupo.addEventListener('pointerenter', mostrar);
        grupo.addEventListener('focus', mostrar);
        grupo.addEventListener('pointerleave', caixaDica.esconder);
        grupo.addEventListener('blur', caixaDica.esconder);
        desenho.append(grupo);
    });

    recipiente.append(desenho);
}

// Barras horizontais: uma linha por item, uma barra por série, com o valor na ponta
function graficoBarras(recipiente, {itens, series}) {
    recipiente.replaceChildren(legenda(series));
    const maximo = Math.max(...series.flatMap(s => s.valores), 0) || 1;
    const lista = criar('div', 'grafico-barras');

    itens.forEach((item, i) => {
        lista.append(criar('div', 'grafico-barras-nome', item));
        const barras = criar('div');
        for (const serie of series) {
            const linha = criar('div', 'grafico-barra');
            linha.title = `${serie.nome}: ${moeda.format(serie.valores[i])}`;
            const barra = criar('i');
            barra.style.background = serie.cor;
            // Reserva 96px da linha para o valor escrito na ponta
            barra.style.width = `calc((100% - 96px) * ${serie.valores[i] / maximo})`;
            if (!serie.valores[i]) barra.hidden = true;
            linha.append(barra, criar('span', '', moeda.format(serie.valores[i])));
            barras.append(linha);
        }
        lista.append(barras);
    });
    recipiente.append(lista);
}

function semDados(recipiente, texto) {
    recipiente.replaceChildren(criar('p', 'grafico-vazio', texto));
}

let dadosFinanceiro = null;

function desenharGraficos(financeiro = dadosFinanceiro) {
    dadosFinanceiro = financeiro;
    // Com a aba escondida não há largura para medir; o desenho acontece quando ela abre
    if (!financeiro || $('financeiro').hidden) return;

    const estilo = getComputedStyle(document.documentElement);
    const cor = nome => estilo.getPropertyValue(nome).trim();
    const compras = {nome: 'Compras', cor: cor('--serie-compras')};
    const vendas = {nome: 'Vendas', cor: cor('--serie-vendas')};

    const meses = financeiro.meses;
    if (meses.every(m => !Number(m.compras) && !Number(m.vendas))) {
        semDados($('grafico-meses'), 'Sem compras ou vendas nos últimos 12 meses.');
        semDados($('grafico-resultado'), 'Sem compras ou vendas nos últimos 12 meses.');
    } else {
        const datas = meses.map(m => new Date(m.mes + '-01T00:00:00'));
        const eixo = {
            categorias: datas.map(d => d.toLocaleDateString('pt-BR', {month: 'short'}).replace('.', '')),
            rotulosLongos: datas.map(d => d.toLocaleDateString('pt-BR', {month: 'long', year: 'numeric'}))
        };
        graficoColunas($('grafico-meses'), {
            ...eixo,
            series: [
                {...compras, valores: meses.map(m => Number(m.compras))},
                {...vendas, valores: meses.map(m => Number(m.vendas))}
            ]
        });
        graficoColunas($('grafico-resultado'), {
            ...eixo,
            series: [{
                nome: 'Resultado',
                valores: meses.map(m => m.vendas - m.compras),
                cor: valor => cor(valor < 0 ? '--prejuizo' : '--lucro')
            }]
        });
    }

    const movimentados = financeiro.materiais
        .filter(m => Number(m.valorComprado) || Number(m.valorVendido))
        .sort((a, b) => (b.valorComprado + b.valorVendido) - (a.valorComprado + a.valorVendido));
    if (movimentados.length === 0) {
        semDados($('grafico-materiais'), 'Nenhum material com compras ou vendas ainda.');
    } else {
        graficoBarras($('grafico-materiais'), {
            itens: movimentados.map(m => m.materialNome),
            series: [
                {...compras, valores: movimentados.map(m => Number(m.valorComprado))},
                {...vendas, valores: movimentados.map(m => Number(m.valorVendido))}
            ]
        });
    }
}

let redesenho;
window.addEventListener('resize', () => {
    clearTimeout(redesenho);
    redesenho = setTimeout(() => desenharGraficos(), 150);
});
matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => desenharGraficos());
