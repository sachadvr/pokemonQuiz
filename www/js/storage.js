class StorageManager {
    constructor() {
        this.storageKey = 'pokemon_quiz_scores.json';
        this.scores = [];
        this.isCordovaReady = false;
        this.init();
    }

    async init() {
        if (typeof cordova !== 'undefined') {
            document.addEventListener('deviceready', () => {
                this.isCordovaReady = true;
                this.loadScores();
            });
        } else {
            this.loadScoresFallback();
        }
    }

    async loadScores() {
        if (this.isCordovaReady) {
            try {
                const fileEntry = await this.getFileEntry();
                const file = await this.getFile(fileEntry);
                const content = await this.readFile(file);
                this.scores = content ? JSON.parse(content) : [];
            } catch (error) {
                this.loadScoresFallback();
            }
        } else {
            this.loadScoresFallback();
        }
    }

    loadScoresFallback() { //If i cannot use cordova plugin use localStorage
        try {
            const stored = localStorage.getItem(this.storageKey);
            this.scores = stored ? JSON.parse(stored) : [];
        } catch (error) {
            this.scores = [];
        }
    }

    async saveScores() {
        if (this.isCordovaReady) {
            try {
                const fileEntry = await this.getFileEntry();
                const writer = await this.getWriter(fileEntry);
                const data = JSON.stringify(this.scores);
                await this.writeFile(writer, data);
                return true;
            } catch (error) {
                return this.saveScoresFallback();
            }
        } else {
            return this.saveScoresFallback();
        }
    }

    saveScoresFallback() {
        try {
            localStorage.setItem(this.storageKey, JSON.stringify(this.scores));
            return true;
        } catch (error) {
            return false;
        }
    }

    async getFileEntry() {
        return new Promise((resolve, reject) => {
            window.resolveLocalFileSystemURL(
                cordova.file.dataDirectory,
                (dirEntry) => {
                    dirEntry.getFile(
                        this.storageKey,
                        { create: true, exclusive: false },
                        resolve,
                        reject
                    );
                },
                reject
            );
        });
    }

    async getFile(fileEntry) {
        return new Promise((resolve, reject) => {
            fileEntry.file(resolve, reject);
        });
    }

    async readFile(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsText(file);
        });
    }

    async getWriter(fileEntry) {
        return new Promise((resolve, reject) => {
            fileEntry.createWriter(resolve, reject);
        });
    }

    async writeFile(writer, data) {
        return new Promise((resolve, reject) => {
            writer.onwriteend = resolve;
            writer.onerror = reject;
            writer.write(data);
        });
    }

    async addScore(playerName, score, time) {
        const newScore = {
            id: Date.now(),
            playerName: playerName.trim(),
            score: parseInt(score),
            time: parseInt(time),
            date: new Date().toISOString(),
            timestamp: Date.now()
        };

        this.scores.push(newScore);
        this.scores.sort((a, b) => {
            if (b.score !== a.score) {
                return b.score - a.score;
            }
            return a.time - b.time;
        });

        this.scores = this.scores.slice(0, 50);
        await this.saveScores();
        return newScore;
    }

    getTopScores(limit = 5) {
        return this.scores.slice(0, limit);
    }

    getAllScores() {
        return [...this.scores];
    }

    clearScores() {
        this.scores = [];
        this.saveScores();
    }

    getPlayerBestScore(playerName) {
        const playerScores = this.scores.filter(score => 
            score.playerName.toLowerCase() === playerName.toLowerCase()
        );
        
        if (playerScores.length === 0) return null;
        
        return playerScores.reduce((best, current) => {
            if (current.score > best.score) return current;
            if (current.score === best.score && current.time < best.time) return current;
            return best;
        });
    }

    formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    }
}

window.StorageManager = StorageManager;
