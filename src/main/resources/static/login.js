const $ = id => document.getElementById(id);

// O servidor entrega o token CSRF em cookie e espera recebê-lo de volta neste cabeçalho
function cabecalhoCsrf() {
    const token = document.cookie.split('; ').find(c => c.startsWith('XSRF-TOKEN='));
    return token ? {'X-XSRF-TOKEN': decodeURIComponent(token.split('=')[1])} : {};
}

async function enviar(caminho, corpo, cabecalhos = {}) {
    const resposta = await fetch('/api/auth' + caminho, {method: 'POST', headers: {...cabecalhos, ...cabecalhoCsrf()}, body: corpo});
    if (!resposta.ok) {
        const erro = await resposta.json().catch(() => ({}));
        throw new Error(erro.message || 'Erro ao comunicar com o servidor (' + resposta.status + ')');
    }
}

function avisar(texto, erro = false) {
    const caixa = $('mensagem');
    caixa.textContent = texto;
    caixa.classList.toggle('erro', erro);
    caixa.hidden = false;
}

function entrar(usuario, senha) {
    return enviar('/login', new URLSearchParams({login: usuario, senha}));
}

async function iniciar() {
    const status = await (await fetch('/api/auth/status')).json();
    if (status.autenticado) {
        location.replace('/');
        return;
    }
    $('form-login').hidden = !status.cadastrado;
    $('form-cadastro').hidden = status.cadastrado;
}

function aoEnviar(idFormulario, acao) {
    $(idFormulario).addEventListener('submit', async evento => {
        evento.preventDefault();
        const botao = evento.target.querySelector('[type=submit]');
        botao.disabled = true;
        try {
            await acao();
            location.replace('/');
        } catch (e) {
            avisar(e.message, true);
            botao.disabled = false;
        }
    });
}

aoEnviar('form-login', () => entrar($('login-usuario').value, $('login-senha').value));

aoEnviar('form-cadastro', async () => {
    const usuario = $('cadastro-usuario').value;
    const senha = $('cadastro-senha').value;
    if (senha !== $('cadastro-confirmacao').value) throw new Error('As senhas não são iguais.');

    await enviar('/cadastro', JSON.stringify({login: usuario, senha}), {'Content-Type': 'application/json'});
    await entrar(usuario, senha);
});

iniciar().catch(e => avisar(e.message, true));
