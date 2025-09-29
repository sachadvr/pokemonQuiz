class PokemonAPI {
    constructor() {
        this.baseURL = 'https://tyradex.vercel.app/api/v1/pokemon';
        this.cache = new Map();
        this.pokemonList = [];
        this.isListLoaded = false;
        this.fallbackPokemon = []
    }

    async loadPokemonList() {
        if (this.isListLoaded) {
            return this.pokemonList;
        }

        try {
            if (!navigator.onLine) {
                this.pokemonList = this.fallbackPokemon;
                this.isListLoaded = true;
                return this.pokemonList;
            }

            const response = await fetch(this.baseURL);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.pokemonList = data;
            this.isListLoaded = true;
            return this.pokemonList;
        } catch (error) {
            this.pokemonList = this.fallbackPokemon;
            this.isListLoaded = true;
            return this.pokemonList;
        }
    }

    getRandomPokemon() {
        if (this.pokemonList.length === 0) { // If the pokemon list is not loaded, we use the fallback pokemon
            return this.getRandomFallbackPokemon();
        }

        const randomIndex = Math.floor(Math.random() * this.pokemonList.length);
        const pokemon = this.pokemonList[randomIndex];
        
        this.cache.set(pokemon.id, pokemon);
        return pokemon;
    }

    getRandomFallbackPokemon() {
        const randomIndex = Math.floor(Math.random() * this.fallbackPokemon.length);
        return this.fallbackPokemon[randomIndex];
    }

    async getPokemonById(id) {
        if (this.cache.has(id)) {
            return this.cache.get(id);
        }

        try {
            if (!navigator.onLine) {
                return this.fallbackPokemon.find(p => p.id === id) || this.getRandomFallbackPokemon();
            }

            const response = await fetch(`${this.baseURL}/${id}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const pokemon = await response.json();
            this.cache.set(id, pokemon);
            return pokemon;
        } catch (error) {
            return this.fallbackPokemon.find(p => p.id === id) || this.getRandomFallbackPokemon();
        }
    }

    normalizePokemonName(name) { // We remove here accents and special characters to make the comparison easier
        return name.toLowerCase()
            .replace(/[àáâãäå]/g, 'a')
            .replace(/[èéêë]/g, 'e')
            .replace(/[ìíîï]/g, 'i')
            .replace(/[òóôõö]/g, 'o')
            .replace(/[ùúûü]/g, 'u')
            .replace(/[ç]/g, 'c')
            .replace(/[ñ]/g, 'n')
            .replace(/[^a-z0-9]/g, '');
    }

    checkAnswer(guess, correctName) {
        const normalizedGuess = this.normalizePokemonName(guess);
        const normalizedCorrect = this.normalizePokemonName(correctName);
        
        return normalizedGuess === normalizedCorrect;
    }

    async getQuizPokemon(count = 10) {
        await this.loadPokemonList();
        
        const pokemonList = [];
        const usedIds = new Set();

        for (let i = 0; i < count; i++) {
            let pokemon;
            let attempts = 0;
            const maxAttempts = 20;

            do {
                pokemon = this.getRandomPokemon();
                attempts++;
            } while (usedIds.has(pokemon.id) && attempts < maxAttempts);

            if (pokemon && pokemon.name && pokemon.name.fr) {
                pokemonList.push(pokemon);
                usedIds.add(pokemon.id);
            }
        }

        return pokemonList;
    }
}

window.PokemonAPI = PokemonAPI;
