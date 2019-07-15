export const apps = [{
        id: 'corpus',
        name: 'Corpus',
        description: 'Corpus',
        img: 'how_to_vote',
        url: 'corpus',
        menu: [{
            id: 'corpus',
            name: 'Corpus',
            img: 'how_to_vote',
            url: 'corpus'}]
    }, {
        id: 'tesauro',
        name: 'Tesauro',
        description: 'Tesauro de bioentidades',
        img: 'how_to_vote',
        url: 'thesauro',
        menu: [{
            id: 'corpus',
            name: 'Corpus',
            img: 'how_to_vote',
            url: 'corpus',
            submenu: [{
                id: 'corpus',
                name: 'Corpus',
                img: 'how_to_vote',
                url: 'corpus'
            }, {
                id: 'tesauro',
                name: 'Tesauro',
                img: 'how_to_vote',
                url: 'thesauro'
                }]}]
    }, {
        id: 'search',
        name: 'Buscador',
        description: 'Buscador de Bioentidades',
        img: 'how_to_vote',
        url: 'corpus',
        menu: [{
            id: 'corpus',
            name: 'Corpus',
            img: 'how_to_vote',
            url: 'corpus',
            submenu: [{
                id: 'corpus',
                name: 'Corpus',
                img: 'how_to_vote',
                url: 'corpus'
            }, {
                id: 'tesauro',
                name: 'Tesauro',
                img: 'how_to_vote',
                url: 'thesauro'
            }]}],
        modules: [{
            id: 'corpus',
            name: 'Corpus',
            img: 'how_to_vote',
            url: 'corpus'
        }, {
            id: 'tesauro',
            name: 'Tesauro',
            img: 'how_to_vote',
            url: 'thesauro'
        }]}];
