class NetworkManager {
    constructor() {
        this.isOnline = navigator.onLine;
        this.listeners = [];
        this.init();
    }

    init() {
        window.addEventListener('online', () => {
            this.isOnline = true;
            this.notifyListeners('online');
        });

        window.addEventListener('offline', () => {
            this.isOnline = false;
            this.notifyListeners('offline');
        });

        if (typeof cordova !== 'undefined' && cordova.plugins && cordova.plugins.network) {
            document.addEventListener('deviceready', () => {
                this.isOnline = cordova.plugins.network.type !== 'none';
                this.notifyListeners(this.isOnline ? 'online' : 'offline');
            });
        }
    }

    addListener(callback) {
        this.listeners.push(callback);
    }

    notifyListeners(status) {
        this.listeners.forEach(callback => {
            try {
                callback(status);
            } catch (error) {
                console.warn('Erreur dans le listener réseau:', error);
            }
        });
    }
    getDetailedStatus() {
        if (!this.isOnline) {
            return '🔴 Hors ligne';
        }
        
        return 'En ligne';
    }

    async checkConnection() {
        try {
            const response = await fetch('https://tyradex.vercel.app/api/v1/pokemon', {
                method: 'HEAD',
                timeout: 5000
            });
            this.isOnline = response.ok;
        } catch (error) {
            this.isOnline = false;
        }
        
        this.notifyListeners(this.isOnline ? 'online' : 'offline');
        return this.isOnline;
    }
}

window.NetworkManager = NetworkManager;
