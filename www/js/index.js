class PokemonQuizApp {
    constructor() {
        this.api = new PokemonAPI();
        this.storage = new StorageManager();
        this.network = new NetworkManager();
        
        this.currentScreen = 'home';
        this.quizData = [];
        this.currentQuestion = 0;
        this.score = 0;
        this.startTime = null;
        this.timerInterval = null;
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupNetworkListener();
        this.showScreen('home');
    }

    setupEventListeners() {
        document.getElementById('startQuizBtn').addEventListener('click', () => this.startQuiz());
        document.getElementById('leaderboardBtn').addEventListener('click', () => this.showLeaderboard());
        document.getElementById('backToHomeBtn').addEventListener('click', () => this.showScreen('home'));
        document.getElementById('submitGuessBtn').addEventListener('click', () => this.submitAnswer());
        document.getElementById('pokemonGuess').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.submitAnswer();
        });
        document.getElementById('saveScoreBtn').addEventListener('click', () => this.saveScore());
        document.getElementById('playAgainBtn').addEventListener('click', () => this.startQuiz());
        document.getElementById('viewLeaderboardBtn').addEventListener('click', () => this.showLeaderboard());
        document.getElementById('backToHomeFromLeaderboardBtn').addEventListener('click', () => this.showScreen('home'));
    }

    setupNetworkListener() {
        this.network.addListener((status) => {
            this.updateNetworkDisplay(status);
        });
        
        this.checkApiStatus();
    }

    updateNetworkDisplay(status) {
        const networkStatus = document.getElementById('networkStatus');
        if (!networkStatus) return;

        networkStatus.classList.remove('online', 'offline', 'checking');

        switch (status) {
            case 'online':
                networkStatus.textContent = this.network.getDetailedStatus();
                networkStatus.classList.add('online');
                break;
            case 'offline':
                networkStatus.textContent = '🔴 Hors ligne';
                networkStatus.classList.add('offline');
                break;
            default:
                networkStatus.textContent = 'Vérification...';
                networkStatus.classList.add('checking');
        }
    }

    async checkApiStatus() {
        this.updateNetworkDisplay('checking');
        
        try {
            const isOnline = await this.network.checkConnection();
            this.updateNetworkDisplay(isOnline ? 'online' : 'offline');
        } catch (error) {
            console.warn('Erreur lors de la vérification réseau:', error);
            this.updateNetworkDisplay('offline');
        }
    }

    showScreen(screenName) {
        const screens = ['home', 'quiz', 'results', 'leaderboard'];
        screens.forEach(screen => {
            const element = document.getElementById(`${screen}Screen`);
            if (screen === screenName) {
                element.classList.remove('hidden');
                element.classList.add('active');
            } else {
                element.classList.add('hidden');
                element.classList.remove('active');
            }
        });
        this.currentScreen = screenName;
    }

    async startQuiz() {
        try {
            this.quizData = await this.api.getQuizPokemon(10);
            
            if (!this.quizData || this.quizData.length === 0) {
                throw new Error('Aucune donnée de quiz récupérée');
            }
            
            this.currentQuestion = 0;
            this.score = 0;
            this.startTime = Date.now();
            
            this.showScreen('quiz');
            this.startTimer();
            this.showQuestion();
        } catch (error) {
            console.error('Erreur lors du démarrage du quiz:', error);
            this.showError('Erreur lors du chargement du quiz. Veuillez réessayer.');
        }
    }

    showQuestion() {
        if (this.currentQuestion >= this.quizData.length) {
            this.endQuiz();
            return;
        }

        const pokemon = this.quizData[this.currentQuestion];
        const imageElement = document.getElementById('pokemonImage');
        const guessInput = document.getElementById('pokemonGuess');
        const feedback = document.getElementById('feedback');
        const progressFill = document.getElementById('progressFill');
        const questionCounter = document.getElementById('questionCounter');

        
        imageElement.src = pokemon.sprites.regular;
        imageElement.classList.remove('revealed');
        guessInput.value = '';
        guessInput.disabled = false;
        feedback.classList.add('hidden');
        
        
        const progress = ((this.currentQuestion + 1) / this.quizData.length) * 100;
        progressFill.style.width = `${progress}%`;
        questionCounter.textContent = `Question ${this.currentQuestion + 1}/${this.quizData.length}`;

        
        guessInput.focus();
    }

    async submitAnswer() {
        const guessInput = document.getElementById('pokemonGuess');
        const guess = guessInput.value.trim();
        
        if (!guess) {
            this.showFeedback('Veuillez entrer un nom de Pokémon', 'error');
            return;
        }

        const pokemon = this.quizData[this.currentQuestion];
        const isCorrect = this.api.checkAnswer(guess, pokemon.name.fr);
        
        if (isCorrect) {
            this.score++;
            this.showFeedback('Correct !', 'success');
            document.getElementById('pokemonImage').classList.add('revealed');
        } else {
            this.showFeedback(`Incorrect ! C'était ${pokemon.name.fr}`, 'error');
            document.getElementById('pokemonImage').classList.add('revealed');
        }

        guessInput.disabled = true;
        
        setTimeout(() => {
            this.currentQuestion++;
            this.showQuestion();
        }, 2000);
    }

    showFeedback(message, type) {
        const feedback = document.getElementById('feedback');
        const feedbackText = document.getElementById('feedbackText');
        
        feedbackText.textContent = message;
        feedback.classList.remove('hidden', 'text-green-400', 'text-red-400');
        
        if (type === 'success') {
            feedbackText.classList.add('text-green-400');
            return;
        }
        feedbackText.classList.add('text-red-400');
    }

    endQuiz() {
        this.stopTimer();
        const totalTime = Math.floor((Date.now() - this.startTime) / 1000);
        
        document.getElementById('finalScore').textContent = `${this.score}/${this.quizData.length}`;
        document.getElementById('finalTime').textContent = this.storage.formatTime(totalTime);
        
        this.showScreen('results');
    }

    startTimer() {
        this.timerInterval = setInterval(() => {
            const elapsed = Math.floor((Date.now() - this.startTime) / 1000);
            document.getElementById('timer').textContent = this.storage.formatTime(elapsed);
        }, 1000);
    }

    stopTimer() {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
        }
    }

    async saveScore() {
        const playerNameInput = document.getElementById('playerName');
        const playerName = playerNameInput.value.trim();
        
        if (!playerName) {
            this.showFeedback('Veuillez entrer votre nom', 'error');
            return;
        }

        const totalTime = Math.floor((Date.now() - this.startTime) / 1000);
        const savedScore = await this.storage.addScore(playerName, this.score, totalTime);
        
        if (savedScore) {
            this.showFeedback('Score sauvegardé ! 🎉', 'success');
            playerNameInput.value = '';
        } else {
            this.showFeedback('Erreur lors de la sauvegarde', 'error');
        }
    }

    showLeaderboard() {
        const leaderboardList = document.getElementById('leaderboardList');
        const noScores = document.getElementById('noScores');
        const scores = this.storage.getTopScores(5);
        
        if (scores.length === 0) {
            leaderboardList.innerHTML = '';
            noScores.classList.remove('hidden');
        } else {
            noScores.classList.add('hidden');
            leaderboardList.innerHTML = scores.map((score, index) => {
                const medal = index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : '🏅';
                return `
                    <div class="leaderboard-item">
                        <div class="player-info">
                            <span class="medal">${medal}</span>
                            <div class="player-details">
                                <p class="player-name">${score.playerName}</p>
                                <p class="player-date">${this.storage.formatDate(score.date)}</p>
                            </div>
                        </div>
                        <div class="score-info">
                            <p class="score-value">${score.score}/10</p>
                            <p class="score-time">${this.storage.formatTime(score.time)}</p>
                        </div>
                    </div>
                `;
            }).join('');
        }
        
        this.showScreen('leaderboard');
    }

    showError(message) {
        alert(message);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.app = new PokemonQuizApp();
});

document.addEventListener('deviceready', () => {
    if (window.app) {
        window.app.network.checkConnection();
    }
}, false);
