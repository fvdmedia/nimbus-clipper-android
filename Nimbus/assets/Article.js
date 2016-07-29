Article = {}
Article.win = window;
Article.document = window.document;

//  vars
//  ====

Article.pagesCount = 1; // there's always at least 1 page
Article.afterShowRunThese = [];
Article.debugRemember = [];
Article.debugPrint = function(){};
Article.style = 'html,body,div,span,applet,object,iframe,h1,h2,h3,h4,h5,h6,p,blockquote,pre,a,abbr,acronym,address,big,cite,code,del,dfn,em,img,ins,kbd,q,s,samp,small,strike,strong,sub,sup,tt,var,b,u,i,center,dl,dt,dd,ol,ul,li,fieldset,form,label,legend,table,caption,tbody,tfoot,thead,tr,th,td,article,aside,canvas,details,embed,figure,figcaption,footer,header,hgroup,menu,nav,output,ruby,section,summary,time,mark,audio,video{margin:0;padding:0;border:0;font-size:100%;vertical-align:baseline}article,aside,details,figcaption,figure,footer,header,hgroup,nav,section{display:block}*{margin:0;padding:0}img{border:0}a{font-weight:bold;color:#000}.center{width:60%;min-width:300px;padding:0 10px;margin:20px auto 30px}body{font:16px Arial,Helvetica,sans-serif;color:#000}h1{font-size:2em;font-weight:bold;padding:0 0 10px 0}.author{padding:0 0 10px 0;font-style:italic}.url{padding:0 0 10px 0}article,section,div{line-height:1.5;width:100%;overflow:hidden}p{padding:0 0 10px 0}ul,ol{padding:0 0 20px 20px}table{margin:0 0 10px 0;width:100%;border-collapse:collapse;border-spacing:0}table td,table th{padding:5px;border:1px solid #555;vertical-align:top;text-align:left}img{max-width:100%;height:auto;display:inline-block}';
//  local debug
//  ===========
//  debug in extensions?
//  ====================
Article.debug = false;

Article.nextPage__captionKeywords__not =
        [
            /* english */
            'article', 'story', 'post', 'comment', 'section', 'chapter'
            
        ]
        Article.nextPage__captionKeywords = 
        [
            /* english */
            'next page', 'next',
            
            /* german */
            'vorw&#228;rts', 'weiter',

            /* japanese */
            '&#27425;&#12408;'
        ];
Article.debugOutline = function(){};
Article.log = function(){console.log(arguments)};
//  debug
//  =====
//  random number
Article.rand = function (_min, _max) {
    return (Math.floor(Math.random() * (_max - _min + 1)) + _min);
};

Article.browser = 'chrome';
//  possible values -- in this order
//  ===============
/*
firefox
safari
chrome
internet_explorer
opera
iphone
ipad
android
dolphin
firefox_mobile
chrome_mobile
windows_phone
*/

Article.language = 'general';
//  text used
Article.textForlanguageDetection = Article.document.title;
//  add a couple of random paragraphs, divs
//  ============
var _ps = Article.document.getElementsByTagName('p'),
    _ds = Article.document.getElementsByTagName('div');
/*for (var i = 0; i < 5; i++) {
    Article.textForlanguageDetection += ' ' + $(_ps[Math.floor(Math.random() * _ps.length)]).text();
}
for (var i = 0; i < 5; i++) {
    Article.textForlanguageDetection += ' ' + $(_ds[Math.floor(Math.random() * _ds.length)]).text();
}*/
for (var i = 0; i < 5; i++) {
		Article.textForlanguageDetection += ' ' + $(_ps[Math.floor(Math.random() * _ps.length)]).text();
		//Article.textForlanguageDetection += ' ' + (_ps[Math.floor(Math.random() * _ps.length)]).innerText;
	}
	for (var i = 0; i < 5; i++) {
		Article.textForlanguageDetection += ' ' + $(_ds[Math.floor(Math.random() * _ds.length)]).text();
		//Article.textForlanguageDetection += ' ' + (_ds[Math.floor(Math.random() * _ds.length)]).innerText;
}
//  tidy up
//  =======
Article.textForlanguageDetection = Article.textForlanguageDetection.replace(/<([^>]+?)>/gi, '');
Article.textForlanguageDetection = Article.textForlanguageDetection.replace(/([ nrt]+)/gi, ' ');
//  check
//  =====
switch (true) {
case (Article.textForlanguageDetection.match(/([u3000])/gi) != null):
case (Article.textForlanguageDetection.match(/([u3001])/gi) != null):
case (Article.textForlanguageDetection.match(/([u3002])/gi) != null):
case (Article.textForlanguageDetection.match(/([u301C])/gi) != null):
    Article.language = 'cjk';
    break;
}
//  in case we stop



//  mouse timeouts
//  ==============
Article.relatedNotes__first__mouseEnter_timeout = false;
Article.relatedNotes__first__mouseLeave_timeout = false;
Article.relatedNotes__second__mouseEnter_timeout = false;
Article.relatedNotes__second__mouseLeave_timeout = false;

Article.highlight__getDeepestTextNode = function (_node) {
    var _n = _node;
    while (true) {
        switch (true) {
        case (_n.nodeType && _n.nodeType == 3):
            return _n;
            //  single child
        case (_n.nodeType && _n.nodeType == 1 && _n.childNodes.length > 0):
            _n = _n.childNodes[0];
            break;
            //  no children but has sibling
        case (_n.nodeType && _n.nodeType == 1 && _n.childNodes.length == 0 && _n.nextSibling):
            _n = _n.nextSibling;
            break;
            //  default
        default:
            return _node;
        }
    }
};
Article.highlight__getCommonAncestorContainerForNodes = function (_node1, _node2, _fallback) {
    var
    _parent1 = _node1,
        _parent2 = _node2;
    while (true) {
        //  next
        _parent1 = _parent1.parentNode;
        _parent2 = _parent2.parentNode;
        //  break
        switch (true) {
        case (!(_parent1)):
        case (!(_parent2)):
        case (_parent1 == _fallback):
        case (_parent2 == _fallback):
            return _fallback;
        }
        //  maybe
        switch (true) {
        case (_parent1 == _parent2):
            return _parent1;
        case ($.contains(_parent1, _node2)):
            return _parent1;
        case ($.contains(_parent2, _node1)):
            return _parent2;
        case ($.contains(_parent1, _parent2)):
            return _parent1;
        case ($.contains(_parent2, _parent1)):
            return _parent2;
        }
    }
};



//  levenshtein
//  ===========
Article.levenshteinDistance = function (str1, str2) {
    var l1 = str1.length,
        l2 = str2.length;
    if (Math.min(l1, l2) === 0) {
        return Math.max(l1, l2);
    }
    var i = 0,
        j = 0,
        d = [];
    for (i = 0; i <= l1; i++) {
        d[i] = [];
        d[i][0] = i;
    }
    for (j = 0; j <= l2; j++) {
        d[0][j] = j;
    }
    for (i = 1; i <= l1; i++) {
        for (j = 1; j <= l2; j++) {
            d[i][j] = Math.min(
                d[i - 1][j] + 1,
                d[i][j - 1] + 1,
                d[i - 1][j - 1] + (str1.charAt(i - 1) === str2.charAt(j - 1) ? 0 : 1)
            );
        }
    }
    return d[l1][l2];
};

//  content
//  =======
//  footnotes
Article.footnotedLinksCount = 0;
//  content mark
Article.gotContent = false;
//  content function
Article.getContent = function () {

    if(!Article.gotContent) {
        Article.gotContent= Article.getContent__find();
    }
    return Article.gotContent;
};
//  options
//  =======

// правила для особых доменов, позиция -1 нужна для удобства, если домен не подошел, никаких правил
// правило: название домена: массив вида - аттрибута
Article.specialDomainsRules = {
    'www.latimes.com':['module']
}
Article.parsingOptions = {
    '_elements_ignore': '|button|input|select|textarea|optgroup|command|datalist|--|frame|frameset|noframes|--|style|link|script|noscript|--|canvas|applet|map|--|marquee|area|base|',
    '_elements_ignore_tag': '|form|fieldset|details|dir|--|center|font|yatag|cite|',
    '_elements_ignore_attrs' : ['Support','share', 'login','signup','signin', 'users', 'comment','gallery','inline','hidden','banner', 'ya_partner', 'ya-partner', 'yandex-direct', 'direct.yandex.ru', 'adzerk','ads-container','ads-ad'],
    '_elements_container': '|body|--|article|section|--|div|--|td|--|li|--|dd|dt|',
    '_elements_self_closing': '|br|hr|--|img|--|col|--|source|--|embed|param|--|iframe|',
    '_elements_visible': '|article|section|--|ul|ol|li|dd|--|table|tr|td|--|div|--|p|--|h1|h2|h3|h4|h5|h6|--|span|',
    '_elements_too_much_content': '|b|i|em|strong|--|h1|h2|h3|h4|h5|--|td|',
    '_elements_link_density': '|div|--|table|ul|ol|--|section|aside|header|',
    '_elements_floating': '|div|--|table|',
    '_elements_above_target_ignore': '|br|--|ul|ol|dl|--|table|',
    '_elements_highlight_protect': '|video|audio|source|--|object|param|embed|',
    '_elements_keep_attributes': {
        'a': ['href', 'title', 'name'],
        'img': ['src', 'width', 'height', 'alt', 'title'],
        'video': ['src', 'width', 'height', 'poster', 'audio', 'preload', 'autoplay', 'loop', 'controls'],
        'audio': ['src', 'preload', 'autoplay', 'loop', 'controls'],
        'source': ['src', 'type'],
        'object': ['data', 'type', 'width', 'height', 'classid', 'codebase', 'codetype'],
        'param': ['name', 'value'],
        'embed': ['src', 'type', 'width', 'height', 'flashvars', 'allowscriptaccess', 'allowfullscreen', 'bgcolor'],
        'iframe': ['src', 'width', 'height', 'frameborder', 'scrolling'],
        'td': ['colspan', 'rowspan'],
        'th': ['colspan', 'rowspan']
    }
};

//  skip links
//  ==========
Article.skipStuffFromDomains__links = [
    'doubleclick.net',
    'fastclick.net',
    'adbrite.com',
    'adbureau.net',
    'admob.com',
    'bannersxchange.com',
    'buysellads.com',
    'impact-ad.jp',
    'atdmt.com',
    'advertising.com',
    'itmedia.jp',
    'microad.jp',
    'serving-sys.com',
    'adplan-ds.com'
];
//  skip images
//  ===========
Article.skipStuffFromDomain__images = [
    'googlesyndication.com',
    'fastclick.net',
    '.2mdn.net',
    'de17a.com',
    'content.aimatch.com',
    'bannersxchange.com',
    'buysellads.com',
    'impact-ad.jp',
    'atdmt.com',
    'advertising.com',
    'itmedia.jp',
    'microad.jp',
    'serving-sys.com',
    'adplan-ds.com'
];
//  keep video
//  ==========
Article.keepStuffFromDomain__video = [
    'youtube.com',
    'youtube-nocookie.com',
    'vimeo.com',
    'hulu.com',
    'yahoo.com',
    'flickr.com',
    'newsnetz.ch'
];
//  length
//  ======
Article.measureText__getTextLength = function (_the_text) {
    var _text = _the_text;

    _text = _text.replace(/[\s\n\r]+/gi, '');
    //_text = _text.replace(/\d+/, '');

    return _text.length;
};


//  word count
//  ==========
Article.measureText__getWordCount = function (_the_text) {
    var _text = _the_text;

    //  do stuff
    //  ========
    _text = _text.replace(/[\s\n\r]+/gi, ' ');

    _text = _text.replace(/([.,?!:;()\[\]'""-])/gi, ' $1 ');

    _text = _text.replace(/([\u3000])/gi, '[=words(1)]');
    _text = _text.replace(/([\u3001])/gi, '[=words(2)]');
    _text = _text.replace(/([\u3002])/gi, '[=words(4)]');
    _text = _text.replace(/([\u301C])/gi, '[=words(2)]');
    _text = _text.replace(/([\u2026|\u2025])/gi, '[=words(2)]');
    _text = _text.replace(/([\u30FB\uFF65])/gi, '[=words(1)]');
    _text = _text.replace(/([\u300C\u300D])/gi, '[=words(1)]');
    _text = _text.replace(/([\u300E\u300F])/gi, '[=words(1)]');
    _text = _text.replace(/([\u3014\u3015])/gi, '[=words(1)]');
    _text = _text.replace(/([\u3008\u3009])/gi, '[=words(1)]');
    _text = _text.replace(/([\u300A\u300B])/gi, '[=words(1)]');
    _text = _text.replace(/([\u3010\u3011])/gi, '[=words(1)]');
    _text = _text.replace(/([\u3016\u3017])/gi, '[=words(1)]');
    _text = _text.replace(/([\u3018\u3019])/gi, '[=words(1)]');
    _text = _text.replace(/([\u301A\u301B])/gi, '[=words(1)]');
    _text = _text.replace(/([\u301D\u301E\u301F])/gi, '[=words(1)]');
    _text = _text.replace(/([\u30A0])/gi, '[=words(1)]');


    //  count
    //  =====
    var
    _count = 0,
        _words_match = _text.match(/([^\s\d]{3,})/gi);

    //  add match
    _count += (_words_match != null ? _words_match.length : 0);

    //  add manual count
    _text.replace(/\[=words\((\d)\)\]/, function (_match, _plus) {
        _count += (5 * parseInt(_plus));
    });


    //  return
    //  ======
    return _count;
};
Article.getContent__exploreNodeAndGetStuff = function (_nodeToExplore, _justExploring) {
    var _global__element_index = 0,
        _global__inside_link = false,
        _global__inside_link__element_index = 0,
        _global__length__above_plain_text = 0,
        _global__count__above_plain_words = 0,
        _global__length__above_links_text = 0,
        _global__count__above_links_words = 0,
        _global__count__above_candidates = 0,
        _global__count__above_containers = 0,
        _global__above__plain_text = '',
        _global__above__links_text = '',
        _return__containers = [],
        _return__candidates = [],
        _return__links = [];
    //  recursive function
    //  ==================
    var _recursive = function (_node) {
        //  increment index
        //  starts with 1
        _global__element_index++;
        var
        _tag_name = (_node.nodeType === 3 ? '#text' : ((_node.nodeType === 1 && _node.tagName && _node.tagName > '') ? _node.tagName.toLowerCase() : '#invalid')),
            _result = {
                '__index': _global__element_index,
                '__node': _node,
                '_is__container': (Article.parsingOptions._elements_container.indexOf('|' + _tag_name + '|') > -1),
                '_is__candidate': false,
                '_is__text': false,
                '_is__link': false,
                '_is__link_skip': false,
                '_is__image_small': false,
                '_is__image_medium': false,
                '_is__image_large': false,
                '_is__image_skip': false,
                '_debug__above__plain_text': _global__above__plain_text,
                '_debug__above__links_text': _global__above__links_text,
                '_length__above_plain_text': _global__length__above_plain_text,
                '_count__above_plain_words': _global__count__above_plain_words,
                '_length__above_links_text': _global__length__above_links_text,
                '_count__above_links_words': _global__count__above_links_words,
                '_length__above_all_text': (_global__length__above_plain_text + _global__length__above_links_text),
                '_count__above_all_words': (_global__count__above_plain_words + _global__count__above_links_words),
                '_count__above_candidates': _global__count__above_candidates,
                '_count__above_containers': _global__count__above_containers,
                '_length__plain_text': 0,
                '_count__plain_words': 0,
                '_length__links_text': 0,
                '_count__links_words': 0,
                '_length__all_text': 0,
                '_count__all_words': 0,
                '_count__containers': 0,
                '_count__candidates': 0,
                '_count__links': 0,
                '_count__links_skip': 0,
                '_count__images_small': 0,
                '_count__images_medium': 0,
                '_count__images_large': 0,
                '_count__images_skip': 0
            };
        //  fast return
        //  ===========
        switch (true) {
        case ((_tag_name == '#invalid')):
        case ((Article.parsingOptions._elements_ignore.indexOf('|' + _tag_name + '|') > -1)):
            return;
        case ((Article.parsingOptions._elements_visible.indexOf('|' + _tag_name + '|') > -1)):
            //  included inline
            //  _node, _tag_name must be defined
            //  will return, if node is hidden
            switch (true) {
            case (_node.offsetWidth > 0):
            case (_node.offsetHeight > 0):
                break;
            default:
                switch (true) {
                case (_node.offsetLeft > 0):
                case (_node.offsetTop > 0):
                    break;
                default:
                    //  exclude inline DIVs -- which, stupidly, don't have a width/height
                    if ((_tag_name == 'div') && ((_node.style.display || $.css(_node, "display")) == 'inline')) {
                        break;
                    }
                    //  it's hidden; exit current scope
                    return;
                }
                break;
            }
            break;
            //  self-closing -- with some exceptions
        case (Article.parsingOptions._elements_self_closing.indexOf('|' + _tag_name + '|') > -1):
            switch (true) {
            case ((_tag_name == 'img')):
                break;
            default:
                return;
            }
            break;
        }
        //  do stuff
        //  ========
        switch (true) {
            //  text node
            //  =========
        case ((_tag_name == '#text')):
            //  mark
            _result._is__text = true;
            //  get
            var _nodeText = _node.nodeValue;
            //  result
            _result._length__plain_text = Article.measureText__getTextLength(_nodeText);
            _result._count__plain_words = Article.measureText__getWordCount(_nodeText);
            if (_global__inside_link) {
                _global__length__above_links_text += _result._length__plain_text;
                _global__count__above_links_words += _result._count__plain_words;
                if (false && Article.debug) {
                    _global__above__links_text += ' ' + _nodeText;
                }
            } else {
                _global__length__above_plain_text += _result._length__plain_text;
                _global__count__above_plain_words += _result._count__plain_words;
                if (false && Article.debug) {
                    _global__above__plain_text += ' ' + _nodeText;
                }
            }
            //  return text
            return _result;
            //  link
            //  ====
        case (_tag_name == 'a'):
            var _href = _node.href;
            //  sanity
            if (_href > '');
            else {
                break;
            }
            if (_href.indexOf);
            else {
                break;
            }
            _result._is__link = true;
            //  skip
            for (var i = 0, _i = Article.skipStuffFromDomains__links.length; i < _i; i++) {
                if (_node.href.indexOf(Article.skipStuffFromDomains__links[i]) > -1) {
                    _result._is__link_skip = true;
                    break;
                }
            }
            //  inside link
            if (_global__inside_link);
            else {
                _global__inside_link = true;
                _global__inside_link__element_index = _result.__index;
            }
            //  done
            _return__links.push(_result);
            break;
            //  image
            //  =====
        case (_tag_name == 'img'):
            //  skip
            //  ====
            if (_node.src && _node.src.indexOf) {
                for (var i = 0, _i = Article.skipStuffFromDomain__images.length; i < _i; i++) {
                    if (_node.src.indexOf(Article.skipStuffFromDomain__images[i]) > -1) {
                        _result._is__image_skip = true;
                        break;
                    }
                }
            }
            //  size
            //  ====
            var _width = $(_node).width(),
                _height = $(_node).height();
            switch (true) {
            case ((_width * _height) >= 50000):
            case ((_width >= 350) && (_height >= 75)):
                _result._is__image_large = true;
                break;
            case ((_width * _height) >= 20000):
            case ((_width >= 150) && (_height >= 150)):
                _result._is__image_medium = true;
                break;
            case ((_width <= 5) && (_height <= 5)):
                _result._is__image_skip = true;
                break;
            default:
                _result._is__image_small = true;
                break;
            }
            break;
        }
        //  child nodes
        //  ===========
        for (var i = 0, _i = _node.childNodes.length; i < _i; i++) {
            var
            _child = _node.childNodes[i],
                _child_result = _recursive(_child);
            //  if false, continue
            //  ==================
            if (_child_result);
            else {
                continue;
            }
            //  add to result
            //  =============
            _result._count__links += _child_result._count__links + (_child_result._is__link ? 1 : 0);
            _result._count__links_skip += _child_result._count__links_skip + (_child_result._is__link_skip ? 1 : 0);
            _result._count__images_small += _child_result._count__images_small + (_child_result._is__image_small ? 1 : 0);
            _result._count__images_medium += _child_result._count__images_medium + (_child_result._is__image_medium ? 1 : 0);
            _result._count__images_large += _child_result._count__images_large + (_child_result._is__image_large ? 1 : 0);
            _result._count__images_skip += _child_result._count__images_skip + (_child_result._is__image_skip ? 1 : 0);
            _result._count__containers += _child_result._count__containers + (_child_result._is__container ? 1 : 0);
            _result._count__candidates += _child_result._count__candidates + (_child_result._is__candidate ? 1 : 0);
            _result._length__all_text += _child_result._length__plain_text + _child_result._length__links_text;
            _result._count__all_words += _child_result._count__plain_words + _child_result._count__links_words;
            //  plain text / link text
            //  ======================
            switch (true) {
            case (_child_result._is__link):
                //  no text to add
                _result._length__links_text += (_child_result._length__plain_text + _child_result._length__links_text);
                _result._count__links_words += (_child_result._count__plain_words + _child_result._count__links_words);
                break;
            default:
                _result._length__plain_text += _child_result._length__plain_text;
                _result._count__plain_words += _child_result._count__plain_words;
                _result._length__links_text += _child_result._length__links_text;
                _result._count__links_words += _child_result._count__links_words;
                break;
            }
        }
        //  after child nodes
        //  =================
        //  mark as not in link anymore
        //  ===========================
        if (true && (_result._is__link) && (_global__inside_link__element_index == _result.__index)) {
            _global__inside_link = false;
            _global__inside_link__element_index = 0;
        }
        //  add to containers
        //  =================
        if (_result._is__container || ((_result.__index == 1) && (_justExploring == true))) {
            //  add to containers
            _return__containers.push(_result);
            //  increase above containers
            if (_result._is__container) {
                _global__count__above_containers++;
            }
            //  add to candidates
            if (_justExploring);
            else {
                switch (true) {
                case ((Article.language != 'cjk') && ((_result._count__links * 2) >= _result._count__plain_words)):
                    /* link ratio */
                case ((Article.language != 'cjk') && (_result._length__plain_text < (65 / 3))):
                    /* text length */
                case ((Article.language != 'cjk') && (_result._count__plain_words < 5)):
                    /* words */
                case ((Article.language == 'cjk') && (_result._length__plain_text < 10)):
                    /* text length */
                case ((Article.language == 'cjk') && (_result._count__plain_words < 2)):
                    /* words */
                    //case (_result._length__plain_text == 0):    /* no text */
                    //case (_result._count__plain_words == 0):    /* no words */
                    //case ((Article.language == 'cjk') && ((_result._length__plain_text / 65 / 3) < 0.1)):             /* paragrahs of 3 lines */
                    //case ((Article.language != 'cjk') && ((_result._count__plain_words / 50) < 0.5)):                 /* paragraphs of 50 words */
                    //  not a valid candidate
                    //if (_tag_name == 'div') { Article.log('bad candidate', _result.__node); }
                    break;
                default:
                    //  good candidate
                    _result._is__candidate = true;
                    _return__candidates.push(_result);
                    //  increase above candidates
                    _global__count__above_candidates++;
                    break;
                }
                //  special case for body -- if it was just skipped
                //  =====================
                if ((_result.__index == 1) && !(_result._is__candidate)) {
                    _result._is__candidate = true;
                    _result._is__bad = true;
                    _return__candidates.push(_result);
                }
            }
        }
        //  return
        //  ======
        return _result;
    };
    //  actually do it
    //  ==============
    _recursive(_nodeToExplore);
    //  just exploring -- return first thing
    //  ==============
    if (_justExploring) {
        return _return__containers.pop();
    }
    //  return containers list
    //  ======================
    return {
        '_containers': _return__containers,
        '_candidates': _return__candidates,
        '_links': _return__links
    };
};
Article.getContent__processCandidates = function (_candidatesToProcess) {
    //  process this var
    //  ================
    var _candidates = _candidatesToProcess;
    //  sort _candidates -- the lower in the dom, the closer to position 0
    //  ================
    _candidates.sort(function (a, b) {
        switch (true) {
        case (a.__index < b.__index):
            return -1;
        case (a.__index > b.__index):
            return 1;
        default:
            return 0;
        }
    });
    //  get first
    //  =========
    var _main = _candidates[0]

    //  pieces of text
    //  and points computation
    //  ======================
    for (var i = 0, _i = _candidates.length; i < _i; i++) {
        //  pieces
        //  ======
        var
        _count__pieces = 0,
            _array__pieces = [];
        for (var k = i, _k = _candidates.length; k < _k; k++) {
            if (_candidates[k]._count__candidates > 0) {
                continue;
            }
            if ($.contains(_candidates[i].__node, _candidates[k].__node));
            else {
                continue;
            }
            //  store piece, if in debug mode
            if (Article.debug) {
                _array__pieces.push(_candidates[k]);
            }
            //  incement pieces count
            _count__pieces++;
        }
        //  candidate details
        //  =================
        _candidates[i]['__candidate_details'] = Article.getContent__computeDetailsForCandidate(_candidates[i], _main);
        //  pieces -- do this here because _main doesn't yet have a pieces count
        //  ======
        //  set pieces
        _candidates[i]['_count__pieces'] = _count__pieces;
        _candidates[i]['_array__pieces'] = _array__pieces;
        //  pieces ratio
        _candidates[i]['__candidate_details']['_ratio__count__pieces_to_total_pieces'] = (_count__pieces / (_candidates[0]._count__pieces + 1));

        _candidates[i].__points_history = Article.getContent__computePointsForCandidate(_candidates[i], _main);
        _candidates[i].__points = _candidates[i].__points_history[0];
    }
    //  sort _candidates -- the more points, the closer to position 0
    //  ================
    _candidates.sort(function (a, b) {
        switch (true) {
        case (a.__points > b.__points):
            return -1;
        case (a.__points < b.__points):
            return 1;
        default:
            return 0;
        }
    });
    //  return
    //  ======
    return _candidates;
};
Article.getContent__computeDetailsForCandidate = function (_e, _main) {
    var _r = {};
    //  bad candidate
    //  =============
    if (_e._is__bad) {
        return _r;
    }
    //  paragraphs
    //  ==========
    _r['_count__lines_of_65_characters'] = (_e._length__plain_text / 65);
    _r['_count__paragraphs_of_3_lines'] = (_r._count__lines_of_65_characters / 3);
    _r['_count__paragraphs_of_5_lines'] = (_r._count__lines_of_65_characters / 5);
    _r['_count__paragraphs_of_50_words'] = (_e._count__plain_words / 50);
    _r['_count__paragraphs_of_80_words'] = (_e._count__plain_words / 80);
    //  total text
    //  ==========
    _r['_ratio__length__plain_text_to_total_plain_text'] = (_e._length__plain_text / _main._length__plain_text);
    _r['_ratio__count__plain_words_to_total_plain_words'] = (_e._count__plain_words / _main._count__plain_words);
    //  links
    //  =====
    _r['_ratio__length__links_text_to_plain_text'] = (_e._length__links_text / _e._length__plain_text);
    _r['_ratio__count__links_words_to_plain_words'] = (_e._count__links_words / _e._count__plain_words);
    _r['_ratio__length__links_text_to_all_text'] = (_e._length__links_text / _e._length__all_text);
    _r['_ratio__count__links_words_to_all_words'] = (_e._count__links_words / _e._count__all_words);
    _r['_ratio__length__links_text_to_total_links_text'] = (_e._length__links_text / (_main._length__links_text + 1));
    _r['_ratio__count__links_words_to_total_links_words'] = (_e._count__links_words / (_main._count__links_words + 1));
    _r['_ratio__count__links_to_total_links'] = (_e._count__links / (_main._count__links + 1));
    _r['_ratio__count__links_to_plain_words'] = ((_e._count__links * 2) / _e._count__plain_words);
    //  text above
    //  ==========
    var
    _divide__candidates = Math.max(2, Math.ceil(_e._count__above_candidates * 0.5)),
        _above_text = ((0 + (_e._length__above_plain_text * 1) + (_e._length__above_plain_text / _divide__candidates)) / 2),
        _above_words = ((0 + (_e._count__above_plain_words * 1) + (_e._count__above_plain_words / _divide__candidates)) / 2);
    _r['_ratio__length__above_plain_text_to_total_plain_text'] = (_above_text / _main._length__plain_text);
    _r['_ratio__count__above_plain_words_to_total_plain_words'] = (_above_words / _main._count__plain_words);
    //  candidates
    //  ==========
    _r['_ratio__count__candidates_to_total_candidates'] = (_e._count__candidates / (_main._count__candidates + 1));
    _r['_ratio__count__containers_to_total_containers'] = (_e._count__containers / (_main._count__containers + 1));
    //  return
    //  ======
    return _r;
};
Article.getContent__computePointsForCandidate = function (_e, _main) {
    var
    _details = _e.__candidate_details,
        _points_history = [],
        _really_big = ((_main._length__plain_text / 65) > 250);
    //  bad candidate
    if (_e._is__bad) {
        return [0];
    }
    //  the basics
    //  ==========
    _points_history.unshift(((0 + (_details._count__paragraphs_of_3_lines) + (_details._count__paragraphs_of_5_lines * 1.5) + (_details._count__paragraphs_of_50_words) + (_details._count__paragraphs_of_80_words * 1.5) + (_e._count__images_large * 3) - ((_e._count__images_skip + _e._count__images_small) * 0.5)) * 1000));
    //  negative
    if (_points_history[0] < 0) {
        return [0];
    }
    //  candidates, containers, pieces
    //  ==============================
    var
    _divide__pieces = Math.max(5, Math.ceil(_e._count__pieces * 0.25)),
        _divide__candidates = Math.max(5, Math.ceil(_e._count__candidates * 0.25)),
        _divide__containers = Math.max(10, Math.ceil(_e._count__containers * 0.25));
    _points_history.unshift(((0 + (_points_history[0] * 3) + (_points_history[0] / _divide__pieces) + (_points_history[0] / _divide__candidates) + (_points_history[0] / _divide__containers)) / 6));
    //  total text
    //  ==========
    Article.getContent__computePointsForCandidate__do(0.10, 2, (1 - (1 - _details._ratio__length__plain_text_to_total_plain_text)), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 2, (1 - (1 - _details._ratio__count__plain_words_to_total_plain_words)), _points_history);
    if (_really_big) {
        Article.getContent__computePointsForCandidate__do(0.10, 4, (1 - (1 - _details._ratio__length__plain_text_to_total_plain_text)), _points_history);
        Article.getContent__computePointsForCandidate__do(0.10, 4, (1 - (1 - _details._ratio__count__plain_words_to_total_plain_words)), _points_history);
    }
    //  text above
    //  ==========
    Article.getContent__computePointsForCandidate__do(0.10, 5, (1 - _details._ratio__length__above_plain_text_to_total_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 5, (1 - _details._ratio__count__above_plain_words_to_total_plain_words), _points_history);
    if (_really_big) {
        Article.getContent__computePointsForCandidate__do(0.10, 10, (1 - _details._ratio__length__above_plain_text_to_total_plain_text), _points_history);
        Article.getContent__computePointsForCandidate__do(0.10, 10, (1 - _details._ratio__count__above_plain_words_to_total_plain_words), _points_history);
    }
    //  links outer
    //  ===========
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details._ratio__length__links_text_to_total_links_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details._ratio__count__links_words_to_total_links_words), _points_history);
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details._ratio__count__links_to_total_links), _points_history);
    //  links inner
    //  ===========
    var __lr = (Article.language == 'cjk' ? 0.75 : 0.50);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details._ratio__length__links_text_to_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details._ratio__count__links_words_to_plain_words), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details._ratio__length__links_text_to_all_text), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details._ratio__count__links_words_to_all_words), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details._ratio__count__links_to_plain_words), _points_history);
    //  candidates, containers, pieces
    //  ==============================
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details._ratio__count__candidates_to_total_candidates), _points_history);
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details._ratio__count__containers_to_total_containers), _points_history);
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details._ratio__count__pieces_to_total_pieces), _points_history);
    //  return -- will get [0] as the actual final points
    //  ======
    return _points_history;
};
Article.getContent__processCandidatesSecond = function (_processedCandidates) {
    var
    _candidates = _processedCandidates,
        _main = _candidates[0];
    //  only get children of target
    //  ===========================
    _candidates = $.map(_candidates, function (_element, _index) {
        switch (true) {
        case (!(_index > 0)):
        case (!($.contains(_main.__node, _element.__node))):
            return null;
        default:
            return _element;
        }
    });
    //  add main - to amke sure the result is never blank
    _candidates.unshift(_main);
    //  sort _candidates -- the lower in the dom, the closer to position 0
    //  ================
    _candidates.sort(function (a, b) {
        switch (true) {
        case (a.__index < b.__index):
            return -1;
        case (a.__index > b.__index):
            return 1;
        default:
            return 0;
        }
    });
    //  second candidate computation
    //  ============================
    for (var i = 0, _i = _candidates.length; i < _i; i++) {
        //  additional numbers
        //  ==================
        _candidates[i].__second_length__above_plain_text = (_candidates[i]._length__above_plain_text - _main._length__above_plain_text);
        _candidates[i].__second_count__above_plain_words = (_candidates[i]._count__above_plain_words - _main._count__above_plain_words);
        //  candidate details
        //  =================
        _candidates[i]['__candidate_details_second'] = Article.getContent__computeDetailsForCandidateSecond(_candidates[i], _main);
        //  check some more
        //  ===============

        //  points
        //  ======
        _candidates[i].__points_history_second = Article.getContent__computePointsForCandidateSecond(_candidates[i], _main);
        _candidates[i].__points_second = _candidates[i].__points_history_second[0];
    }
    //  sort _candidates -- the more points, the closer to position 0
    //  ================
    _candidates.sort(function (a, b) {
        switch (true) {
        case (a.__points_second > b.__points_second):
            return -1;
        case (a.__points_second < b.__points_second):
            return 1;
        default:
            return 0;
        }
    });
    //  return
    //  ======
    return _candidates;
};
Article.getContent__computeDetailsForCandidateSecond = function (_e, _main) {
    var _r = {};
    //  bad candidate
    //  =============
    if (_e._is__bad) {
        return _r;
    }
    //  total text
    //  ==========
    _r['_ratio__length__plain_text_to_total_plain_text'] = (_e._length__plain_text / _main._length__plain_text);
    _r['_ratio__count__plain_words_to_total_plain_words'] = (_e._count__plain_words / _main._count__plain_words);
    //  links
    //  =====
    _r['_ratio__length__links_text_to_all_text'] = (_e._length__links_text / _e._length__all_text);
    _r['_ratio__count__links_words_to_all_words'] = (_e._count__links_words / _e._count__all_words);
    _r['_ratio__length__links_text_to_total_links_text'] = (_e._length__links_text / (_main._length__links_text + 1));
    _r['_ratio__count__links_words_to_total_links_words'] = (_e._count__links_words / (_main._count__links_words + 1));
    _r['_ratio__count__links_to_total_links'] = (_e._count__links / (_main._count__links + 1));
    _r['_ratio__count__links_to_plain_words'] = ((_e._count__links * 2) / _e._count__plain_words);
    //  text above
    //  ==========
    var
    _divide__candidates = Math.max(2, Math.ceil((_e._count__above_candidates - _main._count__above_candidates) * 0.5)),
        _above_text = ((0 + (_e.__second_length__above_plain_text * 1) + (_e.__second_length__above_plain_text / _divide__candidates)) / 2),
        _above_words = ((0 + (_e.__second_count__above_plain_words * 1) + (_e.__second_count__above_plain_words / _divide__candidates)) / 2);
    _r['_ratio__length__above_plain_text_to_total_plain_text'] = (_above_text / _main._length__plain_text);
    _r['_ratio__count__above_plain_words_to_total_plain_words'] = (_above_words / _main._count__plain_words);
    _r['_ratio__length__above_plain_text_to_plain_text'] = (_above_text / _e._length__plain_text);
    _r['_ratio__count__above_plain_words_to_plain_words'] = (_above_words / _e._count__plain_words);
    //  candidates
    //  ==========
    _r['_ratio__count__candidates_to_total_candidates'] = (Math.max(0, (_e._count__candidates - (_main._count__candidates * 0.25))) / (_main._count__candidates + 1));
    _r['_ratio__count__containers_to_total_containers'] = (Math.max(0, (_e._count__containers - (_main._count__containers * 0.25))) / (_main._count__containers + 1));
    _r['_ratio__count__pieces_to_total_pieces'] = (Math.max(0, (_e._count__pieces - (_main._count__pieces * 0.25))) / (_main._count__pieces + 1));
    //  return
    //  ======
    return _r;
};
Article.getContent__computePointsForCandidateSecond = function (_e, _main) {
    var
    _details = _e.__candidate_details,
        _details_second = _e.__candidate_details_second,
        _points_history = [];
    //  bad candidate
    if (_e._is__bad) {
        return [0];
    }
    //  get initial points
    //  ==================
    _points_history.unshift(_e.__points_history[(_e.__points_history.length - 1)]);
    //  candidates, containers, pieces
    //  ==============================
    var
    _divide__pieces = Math.max(5, Math.ceil(_e._count__pieces * 0.25)),
        _divide__candidates = Math.max(5, Math.ceil(_e._count__candidates * 0.25)),
        _divide__containers = Math.max(10, Math.ceil(_e._count__containers * 0.25));
    _points_history.unshift(((0 + (_points_history[0] * 3) + ((_points_history[0] / _divide__pieces) * 2) + ((_points_history[0] / _divide__candidates) * 2) + ((_points_history[0] / _divide__containers) * 2)) / 9));
    //  total text
    //  ==========
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - (1 - _details_second._ratio__length__plain_text_to_total_plain_text)), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - (1 - _details_second._ratio__count__plain_words_to_total_plain_words)), _points_history);
    //  text above
    //  ==========
    var __ar = (Article.language == 'cjk' ? 0.50 : 0.10);
    Article.getContent__computePointsForCandidate__do(__ar, 1, (1 - _details_second._ratio__length__above_plain_text_to_total_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(__ar, 1, (1 - _details_second._ratio__count__above_plain_words_to_total_plain_words), _points_history);
    Article.getContent__computePointsForCandidate__do(__ar, 1, (1 - _details_second._ratio__length__above_plain_text_to_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(__ar, 1, (1 - _details_second._ratio__count__above_plain_words_to_plain_words), _points_history);
    //  links outer
    //  ===========
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details_second._ratio__count__links_to_total_links), _points_history);
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details_second._ratio__length__links_text_to_total_links_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - _details_second._ratio__count__links_words_to_total_links_words), _points_history);
    //  links inner
    //  ===========
    var __lr = (Article.language == 'cjk' ? 0.75 : 0.50);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details._ratio__length__links_text_to_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details._ratio__count__links_words_to_plain_words), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details_second._ratio__length__links_text_to_all_text), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details_second._ratio__count__links_words_to_all_words), _points_history);
    Article.getContent__computePointsForCandidate__do(__lr, 1, (1 - _details_second._ratio__count__links_to_plain_words), _points_history);
    //  candidates, containers, pieces
    //  ==============================
    Article.getContent__computePointsForCandidate__do(0.10, 2, (1 - _details_second._ratio__count__candidates_to_total_candidates), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 2, (1 - _details_second._ratio__count__containers_to_total_containers), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 2, (1 - _details_second._ratio__count__pieces_to_total_pieces), _points_history);
    //  return -- will get [0] as the actual final points
    //  ======
    return _points_history;
};
Article.getContent__computePointsForCandidateThird = function (_e, _main) {
    var
    _details = _e.__candidate_details,
        _details_second = _e.__candidate_details_second,
        _points_history = [];
    //  bad candidate
    if (_e._is__bad) {
        return [0];
    }
    //  get initial points
    //  ==================
    _points_history.unshift(_e.__points_history[(_e.__points_history.length - 1)]);
    //  candidates, containers, pieces
    //  ==============================
    var
    _divide__pieces = Math.max(2, Math.ceil(_e._count__pieces * 0.25)),
        _divide__candidates = Math.max(2, Math.ceil(_e._count__candidates * 0.25)),
        _divide__containers = Math.max(4, Math.ceil(_e._count__containers * 0.25));
    _points_history.unshift(((0 + (_points_history[0] * 3) + ((_points_history[0] / _divide__pieces) * 2) + ((_points_history[0] / _divide__candidates) * 2) + ((_points_history[0] / _divide__containers) * 2)) / 9));
    //  total text
    //  ==========
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - (1 - _details_second._ratio__length__plain_text_to_total_plain_text)), _points_history);
    Article.getContent__computePointsForCandidate__do(0.75, 1, (1 - (1 - _details_second._ratio__count__plain_words_to_total_plain_words)), _points_history);
    //  text above
    //  ==========
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__length__above_plain_text_to_total_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__count__above_plain_words_to_total_plain_words), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 1, (1 - _details_second._ratio__length__above_plain_text_to_total_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 1, (1 - _details_second._ratio__count__above_plain_words_to_total_plain_words), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 1, (1 - _details_second._ratio__length__above_plain_text_to_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.10, 1, (1 - _details_second._ratio__count__above_plain_words_to_plain_words), _points_history);
    //  links inner
    //  ===========
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__length__links_text_to_all_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__count__links_words_to_all_words), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__length__links_text_to_plain_text), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__count__links_words_to_plain_words), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__count__links_to_plain_words), _points_history);
    //  candidates, containers, pieces
    //  ==============================
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__count__candidates_to_total_candidates), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__count__containers_to_total_containers), _points_history);
    Article.getContent__computePointsForCandidate__do(0.50, 1, (1 - _details._ratio__count__pieces_to_total_pieces), _points_history);
    //  return -- will get [0] as the actual final points
    //  ======
    return _points_history;
};
Article.getContent__computePointsForCandidate__do = function (_ratio_remaining, _power, _ratio, _points_history) {
    var
    _points_remaining = (_points_history[0] * _ratio_remaining),
        _points_to_compute = (_points_history[0] - _points_remaining);
    if (_ratio < 0) {
        //_points_return = (0.75 * _points_remaining);
        _points_return = _points_remaining;
    } else {
        _points_return = 0 + _points_remaining + (_points_to_compute * Math.pow(_ratio, _power));
    }
    //  add
    _points_history.unshift(_points_return);
};
Article.getContent__buildHTMLForNode = function (_nodeToBuildHTMLFor, _custom_mode) {
    

    var
    _global__element_index = 0,
        _global__the_html = '',
        _global__exploreNodeToBuildHTMLFor = Article.getContent__exploreNodeAndGetStuff(_nodeToBuildHTMLFor, true);

    //  custom
    //  ======
    switch (_custom_mode) {
    case 'above-the-target':
        _global__exploreNodeToBuildHTMLFor = false;
        break;
    }

    //  recursive function
    //  ==================
    var _recursive = function (_node) {
        //  increment index -- starts with 1
        //  ===============

        _global__element_index++;

        //  vars
        //  ====
        var
        _explored = false,
            _tag_name = (_node.nodeType === 3 ? '#text' : ((_node.nodeType === 1 && _node.tagName && _node.tagName > '') ? _node.tagName.toLowerCase() : '#invalid')),
            _pos__start__before = 0,
            _pos__start__after = 0,
            _pos__end__before = 0,
            _pos__end__after = 0;

        //  fast return
        //  ===========
        switch (true) {
        case ((_tag_name == '#invalid')):
        case ((Article.parsingOptions._elements_ignore.indexOf('|' + _tag_name + '|') > -1)):
            return;

        case (_tag_name == '#text'):
            _global__the_html += _node.nodeValue
                .replace(/</gi, '&lt;')
                .replace(/>/gi, '&gt;');
            return;
        }

        //  hidden
        //  ======
        if (Article.parsingOptions._elements_visible.indexOf('|' + _tag_name + '|') > -1) {
            //  included inline
            //  _node, _tag_name must be defined
            //  will return, if node is hidden

            switch (true) {
            case (_node.offsetWidth > 0):
            case (_node.offsetHeight > 0):
                break;

            default:
                switch (true) {
                case (_node.offsetLeft > 0):
                case (_node.offsetTop > 0):
                    break;

                default:
                    //  exclude inline DIVs -- which, stupidly, don't have a width/height
                    if ((_tag_name == 'div') && ((_node.style.display || $.css(_node, "display")) == 'inline')) {
                        break;
                    }

                    //  it's hidden; exit current scope
                    return;
                }
                break;
            }
        }

        //  clean -- before
        //  =====

        //  just a return will skip the whol element
        //  including children

        //  objects, embeds, iframes
        //  ========================
        switch (_tag_name) {
        case ('object'):
        case ('embed'):
        case ('iframe'):
            var
            _src = (_tag_name == 'object' ? $(_node).find("param[name='movie']").attr('value') : $(_node).attr('src')),
                _skip = ((_src > '') ? false : true);
                
            if (_skip);
            else {
                //  default skip
                _skip = true;

                //  loop
                for (var i = 0, _i = Article.keepStuffFromDomain__video.length; i < _i; i++) {
                    if (_src.indexOf(Article.keepStuffFromDomain__video[i]) > -1) {
                        _skip = false;
                        break;
                    }
                }
            }

            //  skip?
            if (_skip) {
                
                return;
            }

            break;
        }
        
        //  skipped link
        //  ============
        if (_tag_name == 'a' || _tag_name == 'li') {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            switch (true) {
            case (_explored._is__link_skip):
            case (((_explored._count__images_small + _explored._count__images_skip) > 0) && (_explored._length__plain_text < 65)):
                Article.debugOutline(_node, 'clean-before', 'skip-link');
                return;
            }
        }
        
        //  link density
        //  ============
        if (Article.parsingOptions._elements_link_density.indexOf('|' + _tag_name + '|') > -1) {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            switch (true) {
            case (_explored._length__plain_text > (65 * 3 * 2)):
            case (Article.language == 'cjk' && (_explored._length__plain_text > (65 * 3 * 1))):
            case (!(_explored._count__links > 1)):
            case (_global__exploreNodeToBuildHTMLFor && (_explored._length__plain_text / _global__exploreNodeToBuildHTMLFor._length__plain_text) > 0.5):
            case (_global__exploreNodeToBuildHTMLFor && (_explored._count__plain_words / _global__exploreNodeToBuildHTMLFor._count__plain_words) > 0.5):
            case ((_explored._length__plain_text == 0) && (_explored._count__links == 1) && (_explored._length__links_text < 65)):
            case ((_explored._length__plain_text < 25) && ((_explored._count__images_large + _explored._count__images_medium) > 0)):
                break;

            case ((_explored._length__links_text / _explored._length__all_text) < 0.5):
                if (_explored._count__links > 0);
                else {
                    break;
                }
                if (_explored._count__links_skip > 0);
                else {
                    break;
                }
                if (((_explored._count__links_skip / _explored._count__links) > 0.25) && (_explored._length__links_text / _explored._length__all_text) < 0.05) {
                    break;
                }

            default:
                Article.debugOutline(_node, 'clean-before', 'link-density');
                return;
            }
        }
        
        //  floating
        //  ========
        if (Article.parsingOptions._elements_floating.indexOf('|' + _tag_name + '|') > -1) {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            switch (true) {
            case (_explored._length__plain_text > (65 * 3 * 2)):
            case (Article.language == 'cjk' && (_explored._length__plain_text > (65 * 3 * 1))):
            case (_global__exploreNodeToBuildHTMLFor && (_explored._length__plain_text / _global__exploreNodeToBuildHTMLFor._length__plain_text) > 0.25):
            case (_global__exploreNodeToBuildHTMLFor && (_explored._count__plain_words / _global__exploreNodeToBuildHTMLFor._count__plain_words) > 0.25):
            case ((_explored._length__plain_text < 25) && (_explored._length__links_text < 25) && ((_explored._count__images_large + _explored._count__images_medium) > 0)):
            case (_node.getElementsByTagName && (_explored._length__plain_text < (65 * 3 * 1)) && ((_node.getElementsByTagName('h1').length + _node.getElementsByTagName('h2').length + _node.getElementsByTagName('h3').length + _node.getElementsByTagName('h4').length) > 0)):
                break;

            default:
                var _float = $(_node).css('float');
                if (_float == 'left' || _float == 'right');
                else {
                    break;
                }
                if ((_explored._length__links_text == 0) && ((_explored._count__images_large + _explored._count__images_medium) > 0)) {
                    break;
                }

                Article.debugOutline(_node, 'clean-before', 'floating');
                return;
            }
        }

        

        //  above target
        //  ============
        if (_custom_mode == 'above-the-target') {
            //  is ignored?
            if (Article.parsingOptions._elements_above_target_ignore.indexOf('|' + _tag_name + '|') > -1) {
                Article.debugOutline(_node, 'clean-before', 'above-target');
                return;
            }

            //  is image?
            if (_tag_name == 'img') {
                _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
                if (_explored._is__image_large);
                else {
                    Article.debugOutline(_node, 'clean-before', 'above-target');
                    return;
                }
            }

            //  has too many links?
            //if (_node.getElementsByTagName && _node.getElementsByTagName('a').length > 5)
            //    { Article.debugOutline(_node, 'clean-before', 'above-target'); return; }
        }
        
        //  headers that are images
        //  =======================
        if (_tag_name.match(/^h(1|2|3|4|5|6)$/gi)) {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            switch (true) {
            case ((_explored._length__plain_text < 10) && ((_explored._count__images_small + _explored._count__images_medium + _explored._count__images_large + _explored._count__images_skip) > 0)):
                Article.debugOutline(_node, 'clean-before', 'skip-heading');
                return;
            }
        }

        
        var _rules = Article.specialDomainsRules[Article.document.domain];
        if(_rules) {
            for (var i = _node.attributes.length - 1; i >= 0; i--) {
                var _maybeIgnore = _node.attributes[i].value;
                for (var i = _rules.length - 1; i >= 0; i--) {
                    var _attrValu = _rules[i];
                    if(_maybeIgnore.indexOf(_attrValu) > -1) {
                        return '';
                    }
                };
            };
        }
        


        
            var _ignoreAttrsYes = false;
            // ignore tags with defined attributs
            for (var i = _node.attributes.length - 1; i >= 0; i--) {
                var _maybeIgnore = _node.attributes[i].value;
                for (var i = Article.parsingOptions._elements_ignore_attrs.length - 1; i >= 0; i--) {
                    var _attrValu = Article.parsingOptions._elements_ignore_attrs[i];
                    if(_maybeIgnore.indexOf(_attrValu) > -1) {
                        _ignoreAttrsYes = true;
                        return '';
                    }
                };
            };

            
        //  start tag
        //  =========
        if (Article.parsingOptions._elements_ignore_tag.indexOf('|' + _tag_name + '|') > -1){
            ;
        } else if(_ignoreAttrsYes) {
            ;
        } else {
            /* mark */
            _pos__start__before = _global__the_html.length;
            /* add */
            _global__the_html += '<' + _tag_name;

            //  attributes
            //  ==========

            //  allowed attributes
            //  ==================
            if (_tag_name in Article.parsingOptions._elements_keep_attributes) {
                for (var i = 0, _i = Article.parsingOptions._elements_keep_attributes[_tag_name].length; i < _i; i++) {
                    var
                    _attribute_name = Article.parsingOptions._elements_keep_attributes[_tag_name][i],
                        _attribute_value = _node.getAttribute(_attribute_name);

                    //  if present
                    if (_attribute_value > '') {
                        _global__the_html += ' ' + _attribute_name + '="' + (_attribute_value) + '"';
                    }
                }
            }



            //  keep ID for all elements
            //  ========================
            var _id_attribute = _node.getAttribute('id');
            if (_id_attribute > '') {
                _global__the_html += ' id="' + _id_attribute + '"';
            }

            //  keep class for all elements
            //  ========================
            var _id_attribute = _node.getAttribute('class');
            if (_id_attribute > '') {
                _global__the_html += ' class="' + _id_attribute + '"';
            }
            
/*
            var _id_attribute = _node.getAttribute('style');
            if (_id_attribute > '') {
                _global__the_html += ' style="' + _id_attribute + '"';
            }
  */



            //  links target NEW
            //  ================
            if (_tag_name == 'a') {
                _global__the_html += ' target="_blank"';
            }


            //  close start
            //  ===========
            if (Article.parsingOptions._elements_self_closing.indexOf('|' + _tag_name + '|') > -1) {
                _global__the_html += ' />';
            } else {
                _global__the_html += '>';
            }

            /* mark */
            _pos__start__after = _global__the_html.length;
        }

        //  child nodes
        //  ===========
        if (Article.parsingOptions._elements_self_closing.indexOf('|' + _tag_name + '|') > -1);
        else {
            for (var i = 0, _i = _node.childNodes.length; i < _i; i++) {
                _recursive(_node.childNodes[i]);
            }
        }

        //  end tag
        //  =======
        switch (true) {
        case ((Article.parsingOptions._elements_ignore_tag.indexOf('|' + _tag_name + '|') > -1)):
            return;

        case ((Article.parsingOptions._elements_self_closing.indexOf('|' + _tag_name + '|') > -1)):
            /* mark */
            _pos__end__before = _global__the_html.length;
            /* mark */
            _pos__end__after = _global__the_html.length;
            break;

        default:
            /* mark */
            _pos__end__before = _global__the_html.length;
            /* end */
            _global__the_html += '</' + _tag_name + '>';
            /* mark */
            _pos__end__after = _global__the_html.length;
            break;
        }

        //  clean -- after
        //  =====

        //  we need to actually cut things out of 
        //  "_global__the_html", for stuff to not be there


        //  largeObject classes
        //  ===================
        if (_tag_name == 'iframe' || _tag_name == 'embed' || _tag_name == 'object') {
            _global__the_html = '' + _global__the_html.substr(0, _pos__start__before) + '<div class="readableLargeObjectContainer">' + _global__the_html.substr(_pos__start__before, (_pos__end__after - _pos__start__before)) + '</div>';
            return;
        }

        //  add image classes
        //  =================
        if (_tag_name == 'img') {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            switch (true) {
            case (_explored._is__image_skip):
                Article.debugOutline(_node, 'clean-after', 'skip-img');
                _global__the_html = _global__the_html.substr(0, _pos__start__before);
                return;

            case (_explored._is__image_large):

                
                _global__the_html = '' + _global__the_html.substr(0, _pos__start__before) + '<div class="readableLargeImageContainer' + (($(_node).width() <= 250) && ($(_node).height() >= 250) ? ' float' : '') + '">' + _global__the_html.substr(_pos__start__before, (_pos__end__after - _pos__start__before)).replace(/width="([^=]+?)"/gi, '').replace(/height="([^=]+?)"/gi, '') + '</div>';
                return;
            }
        }

        //  large images in links
        //  =====================
        if (_tag_name == 'a') {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            switch (true) {
            case (_explored._count__images_large == 1):
                _global__the_html = '' + _global__the_html.substr(0, _pos__start__after - 1) + ' class="readableLinkWithLargeImage">' + _global__the_html.substr(_pos__start__after, (_pos__end__before - _pos__start__after)) + '</a>';
                return;

            case (_explored._count__images_medium == 1):
                _global__the_html = '' + _global__the_html.substr(0, _pos__start__after - 1) + ' class="readableLinkWithMediumImage">' + _global__the_html.substr(_pos__start__after, (_pos__end__before - _pos__start__after)) + '</a>';
                return;
            }
        }

        //  too much content
        //  ================
        if (Article.parsingOptions._elements_too_much_content.indexOf('|' + _tag_name + '|') > -1) {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            switch (true) {
            case (_tag_name == 'h1' && (_explored._length__all_text > (65 * 2))):
            case (_tag_name == 'h2' && (_explored._length__all_text > (65 * 2 * 3))):
            case ((_tag_name.match(/^h(3|4|5|6)$/) != null) && (_explored._length__all_text > (65 * 2 * 5))):
            case ((_tag_name.match(/^(b|i|em|strong)$/) != null) && (_explored._length__all_text > (65 * 5 * 5))):
                Article.debugOutline(_node, 'clean-after', 'too-much-content');
                _global__the_html = '' + _global__the_html.substr(0, _pos__start__before) + _global__the_html.substr(_pos__start__after, (_pos__end__before - _pos__start__after));
                return;
            }
        }

        //  empty elements
        //  ==============
        switch (true) {
        case ((Article.parsingOptions._elements_self_closing.indexOf('|' + _tag_name + '|') > -1)):
        case ((Article.parsingOptions._elements_ignore_tag.indexOf('|' + _tag_name + '|') > -1)):
        case (_tag_name == 'td'):
            break;

        default:
            var _contents = _global__the_html.substr(_pos__start__after, (_pos__end__before - _pos__start__after));
            _contents = _contents.replace(/(<br \/>)/gi, '');
            _contents = _contents.replace(/(<hr \/>)/gi, '');

            //  for rows, clear empty cells
            if (_tag_name == 'tr') {
                _contents = _contents.replace(/<td[^>]*?>/gi, '');
                _contents = _contents.replace(/<\/td>/gi, '');
            }

            //  for tables, clear empty rows
            if (_tag_name == 'table') {
                _contents = _contents.replace(/<tr[^>]*?>/gi, '');
                _contents = _contents.replace(/<\/tr>/gi, '');
            }

            var _contentsLength = Article.measureText__getTextLength(_contents);

            switch (true) {
            case (_contentsLength == 0 && _tag_name == 'p'):
                _global__the_html = _global__the_html.substr(0, _pos__start__before) + '<br /><br />';
                return;

            case (_contentsLength == 0):
            case ((_contentsLength < 5) && (Article.parsingOptions._elements_visible.indexOf('|' + _tag_name + '|') > -1)):
                Article.debugOutline(_node, 'clean-after', 'blank');
                _global__the_html = _global__the_html.substr(0, _pos__start__before);
                return;
            }
            break;
        }

        //  too much missing
        //  ================
        if (Article.parsingOptions._elements_link_density.indexOf('|' + _tag_name + '|') > -1) {
            _explored = (_explored || Article.getContent__exploreNodeAndGetStuff(_node, true));
            var
            _contents = _global__the_html
                .substr(_pos__start__after, (_pos__end__before - _pos__start__after))
                .replace(/(<([^>]+)>)/gi, ''),
                _contentsLength = Article.measureText__getTextLength(_contents),
                _initialLength = 0 + _explored._length__all_text + (_explored._count__images_small * 10) + (_explored._count__images_skip * 10) + (_node.getElementsByTagName('iframe').length * 10) + (_node.getElementsByTagName('object').length * 10) + (_node.getElementsByTagName('embed').length * 10) + (_node.getElementsByTagName('button').length * 10) + (_node.getElementsByTagName('input').length * 10) + (_node.getElementsByTagName('select').length * 10) + (_node.getElementsByTagName('textarea').length * 10);

            //  too much missing
            switch (true) {
            case (!(_contentsLength > 0)):
            case (!(_initialLength > 0)):
            case (!((_contentsLength / _initialLength) < 0.5)):
            case (!((Article.language == 'cjk') && (_contentsLength / _initialLength) < 0.1)):
            case ((_global__exploreNodeToBuildHTMLFor && ((_explored._length__plain_text / _global__exploreNodeToBuildHTMLFor._length__plain_text) > 0.25))):
            case ((Article.language == 'cjk') && (_global__exploreNodeToBuildHTMLFor && ((_explored._length__plain_text / _global__exploreNodeToBuildHTMLFor._length__plain_text) > 0.1))):
                break;

            default:
                Article.debugOutline(_node, 'clean-after', 'missing-density');
                _global__the_html = _global__the_html.substr(0, _pos__start__before);
                return;
            }
        }


        //  return
        return;
    };

    //  actually do it
    _recursive(_nodeToBuildHTMLFor);
    //  return html
    return _global__the_html;
};

//  article title marker
//  ====================
Article.articleTitleMarker__start = '';
Article.articleTitleMarker__end = '';
//  article title check function
//  ============================
Article.getContent__find__hasIsolatedTitleInHTML = function (_html) {
    return (_html.substr(0, Article.articleTitleMarker__start.length) == Article.articleTitleMarker__start);
};


//  article title get function
//  ============================
Article.getContent__find__getIsolatedTitleInHTML = function (_html) {
    //  is it there?
    if (Article.getContent__find__hasIsolatedTitleInHTML(_html));
    else {
        return '';
    }

    //  regex
    var
    _getTitleRegex = new RegExp(Article.articleTitleMarker__start + '(.*?)' + Article.articleTitleMarker__end, 'i'),
        _getTitleMatch = _html.match(_getTitleRegex);

    //  match?
    if (_getTitleMatch);
    else {
        return '';
    }

    //  return
    return _getTitleMatch[1];
};


//  find title in arbitrary html
//  ============================
Article.getContent__find__isolateTitleInHTML = function (_html, _document_title) {
    //  can't just use (h1|h2|h3|etc) -- we want to try them in a certain order
    //  =============================
    var
    _heading_pregs = [
        /<(h1)[^>]*?>([\s\S]+?)<\/\1>/gi,
        /<(h2)[^>]*?>([\s\S]+?)<\/\1>/gi,
        /<(h3|h4|h5|h6)[^>]*?>([\s\S]+?)<\/\1>/gi
    ],
        _secondary_headings = '|h2|h3|h4|h5|h6|',
        _search_document_title = ' ' + _document_title.replace(/<[^>]+?>/gi, '').replace(/\s+/gi, ' ') + ' ';

    //  loop pregs
    //  ==========
    for (var i = 0, _i = _heading_pregs.length; i < _i; i++) {
        //  exec
        var _match = _heading_pregs[i].exec(_html);

        //  return?
        switch (true) {
        case (!(_match)):
        case (!(_heading_pregs[i].lastIndex > -1)):
            //  will continue loop
            break;

        default:

            //  measurements
            var
            _heading_end_pos = _heading_pregs[i].lastIndex,
                _heading_start_pos = (_heading_end_pos - _match[0].length),

                _heading_type = _match[1],
                _heading_text = _match[2].replace(/<\s*br[^>]*>/gi, '').replace(/[\n\r]+/gi, ''),
                _heading_text_plain = _heading_text.replace(/<[^>]+?>/gi, '').replace(/\s+/gi, ' ');
            _heading_length = Article.measureText__getTextLength(_heading_text_plain),
            _heading_words = [],

            _to_heading_text = _html.substr(0, _heading_start_pos),
            _to_heading_length = Article.measureText__getTextLength(_to_heading_text.replace(/<[^>]+?>/gi, '').replace(/\s+/gi, ' '));

            //  return?
            switch (true) {
            case (!(_heading_length > 5)):
            case (!(_heading_length < (65 * 3))):
            case (!(_to_heading_length < (65 * 3 * 2))):
                //  will continue for loop
                break;

            case ((_secondary_headings.indexOf('|' + _heading_type + '|') > -1)):
                //  words in this heading
                _heading_words = _heading_text_plain.split(' ');

                //  count words present in title
                for (var j = 0, _j = _heading_words.length, _matched_words = ''; j < _j; j++) {
                    if (_search_document_title.indexOf(' ' + _heading_words[j] + ' ') > -1) {
                        _matched_words += _heading_words[j] + ' ';
                    }
                }

                //  break continues for loop
                //  nothing goes to switch's default
                //  ================================

                //  no break?
                var _no_break = false;
                switch (true) {
                    //  if it's big enough, and it's a substring of the title, it's good
                case ((_heading_length > 20) && (_search_document_title.indexOf(_heading_text_plain) > -1)):

                    //  if it's slightly smaler, but is exactly at the begging or the end
                case ((_heading_length > 10) && ((_search_document_title.indexOf(_heading_text_plain) == 1) || (_search_document_title.indexOf(_heading_text_plain) == (_search_document_title.length - 1 - _heading_text_plain.length)))):

                    _no_break = true;
                    break;
                }

                //  break?
                var _break = false;
                switch (true) {
                    //  no break?
                case (_no_break):
                    break;


                    // heading too long? -- if not h2
                case ((_heading_length > ((_search_document_title.length - 2) * 2)) && (_heading_type != 'h2')):

                    //  heading long enough?
                case ((_heading_length < Math.ceil((_search_document_title.length - 2) * 0.50))):

                    //  enough words matched?
                case ((_heading_length < 25) && (_matched_words.length < Math.ceil(_heading_length * 0.75))):
                case ((_heading_length < 50) && (_matched_words.length < Math.ceil(_heading_length * 0.65))):
                case ((_matched_words.length < Math.ceil(_heading_length * 0.55))):

                    _break = true;
                    break;
                }

                //  break?
                if (_break) {
                    break;
                }


            default:
                //  this is the title -- do isolation; return
                //  =================
                return ''

                + Article.articleTitleMarker__start + _heading_text + Article.articleTitleMarker__end

                + Article.getContent__find__isolateTitleInHTML__balanceDivsAtStart(_html.substr(_heading_end_pos));
            }

            break;
        }
    }

    //  return unmodified
    return _html;
};

Article.getContent__find__isolateTitleInHTML__balanceDivsAtStart__substrCount = function (_haystack, _needle, _offset, _length) {
    // http://kevin.vanzonneveld.net
    // +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   bugfixed by: Onno Marsman
    // +   improved by: Brett Zamir (http://brett-zamir.me)
    // +   improved by: Thomas
    // *     example 1: substr_count('Kevin van Zonneveld', 'e');
    // *     returns 1: 3
    // *     example 2: substr_count('Kevin van Zonneveld', 'K', 1);
    // *     returns 2: 0
    // *     example 3: substr_count('Kevin van Zonneveld', 'Z', 0, 10);
    // *     returns 3: false

    var cnt = 0;

    _haystack += '';
    _needle += '';
    if (isNaN(_offset)) {
        _offset = 0;
    }
    if (isNaN(_length)) {
        _length = 0;
    }
    if (_needle.length == 0) {
        return false;
    }

    _offset--;

    while ((_offset = _haystack.indexOf(_needle, _offset + 1)) != -1) {
        if (_length > 0 && (_offset + _needle.length) > _length) {
            return false;
        }
        cnt++;
    }

    return cnt;
};

Article.getContent__find__isolateTitleInHTML__balanceDivsAtStart = function (_html) {
    //  easy; remove all </X> at begining
    var
    _h = _html.replace(/^(\s*<\s*\/\s*[^>]+>)+/gi, ''),
        _r = /<\s*\/\s*([^\s>]+?)[^>]*>/gi,
        _the_end_tag = '</div>',
        _the_start_tag = '<div',
        _end_tag_pos = -1,
        _last_pos = 0;

    //  remove all unbalanced _end_tags
    for (var _i = 0; _i < 100; _i++) {
        _end_tag_pos = _h.indexOf(_the_end_tag, _last_pos);
        if (_end_tag_pos > -1);
        else {
            break;
        }

        var
        _sub = _h.substr(0, _end_tag_pos),
            _start_tags = Article.getContent__find__isolateTitleInHTML__balanceDivsAtStart__substrCount(_sub, _the_start_tag, _last_pos),
            _end_tags = ((_start_tags > 0) ? (1 + Article.getContent__find__isolateTitleInHTML__balanceDivsAtStart__substrCount(_sub, _the_end_tag, _last_pos)) : false);

        if ((!(_start_tags > 0)) || (_start_tags < _end_tags)) {
            _h = '' + _h.substr(0, _end_tag_pos) + _h.substr(_end_tag_pos + _the_end_tag.length);

            _last_pos = _end_tag_pos;
        } else {
            _last_pos = _end_tag_pos + 1;
        }
    }



    return _h;
};

Article.getContent__find = function () {
    //  get content
    //  ===========
    var
    _found = Article.getContent__findInPage(Article.win),
        _targetNode = _found._targetCandidate.__node,
        _$targetNode = $(_targetNode),
        _aboveNodes = [];

    //  get html
    //  ========
    var
    _foundHTML = _found._html,
        _firstFragmentBefore = Article.getContent__nextPage__getFirstFragment(_foundHTML),
        _documentTitle = (Article.document.title > '' ? Article.document.title : '');
    //  get title
    //  =========
    //  has title already?
    
    var _foundTitle = '';
    //_foundHTML = Article.getContent__find__isolateTitleInHTML(_foundHTML, _documentTitle);
    Article.articleTitle = Article.getContent__find__getIsolatedTitleInHTML(_foundHTML);
        //  get html above?

    if (Article.articleTitle > '');
    else {
        //  get html above target?
        //  ======================
        //  global vars:
        //      _found
        //      _foundHTML
        //      _documentTitle
        //      _aboveNodes
        var
        _prevNode = _found._targetCandidate.__node,
            _prevHTML = '',
            _aboveHTML = '',
            _differentTargets = (_found._firstCandidate.__node != _found._targetCandidate.__node);
        (function () {
            while (true) {
                //  the end?
                switch (true) {
                case (_prevNode.tagName && (_prevNode.tagName.toLowerCase() == 'body')):
                case (_differentTargets && (_prevNode == _found._firstCandidate.__node)):
                    //  enough is enough
                    return;
                }
                //  up or sideways?
                if (_prevNode.previousSibling);
                else {
                    _prevNode = _prevNode.parentNode;
                    continue;
                }
                //  previous
                _prevNode = _prevNode.previousSibling;
                //  outline -- element might be re-outlined, when buildHTML is invoked
                if (Article.debug) {
                    Article.debugOutline(_prevNode, 'target', 'add-above');
                }
                //  get html; add
                _prevHTML = Article.getContent__buildHTMLForNode(_prevNode, 'above-the-target');
                _aboveHTML = _prevHTML + _aboveHTML;
                _aboveNodes.unshift(_prevNode);
                //  isolate title
                _aboveHTML = Article.getContent__find__isolateTitleInHTML(_aboveHTML, _documentTitle);
                //  finished?
                switch (true) {
                case (Article.measureText__getTextLength(_aboveHTML.replace(/<[^>]+?>/gi, '').replace(/s+/gi, ' ')) > (65 * 3 * 3)):
                case (Article.getContent__find__hasIsolatedTitleInHTML(_aboveHTML)):
                    return;
                }
            }
        })();
        //  is what we found any good?
        //  ==========================
        switch (true) {
        case (Article.getContent__find__hasIsolatedTitleInHTML(_aboveHTML)):
        case (_differentTargets && (_aboveHTML.split('<a ').length < 3) && (Article.measureText__getTextLength(_aboveHTML.replace(/<[^>]+?>/gi, '').replace(/s+/gi, ' ')) < (65 * 3))):
            _foundHTML = _aboveHTML + _foundHTML;

            break;
        default:
            _aboveHTML = '';
            _aboveNodes = [];
            break;
        }
        Article.articleTitle = Article.getContent__find__getIsolatedTitleInHTML(_foundHTML);
        
        //  get document title?
        if (Article.articleTitle > '');
        else {
            //  if all else failed, get document title
            //  ======================================
            //  global vars:
            //      _foundHTML
            //      _documentTitle
            (function () {
                //  return?
                //  =======
                if (_documentTitle > '');
                else {
                    return;
                }
                //  vars
                var
                _doc_title_parts = [],
                    _doc_title_pregs = [
                        /( [-][-] |( [-] )|( [>][>] )|( [<][<] )|( [|] )|( [/] ))/i,
                        /(([:] ))/i
                    ];
                //  loop through pregs
                //  ==================
                for (var i = 0, _i = _doc_title_pregs.length; i < _i; i++) {
                    //  split
                    _doc_title_parts = _documentTitle.split(_doc_title_pregs[i]);
                    //  break if we managed a split
                    if (_doc_title_parts.length > 1) {
                        break;
                    }
                }
                //  sort title parts -- longer goes higher up -- i.e. towards 0
                //  ================
                _doc_title_parts.sort(function (a, b) {
                    switch (true) {
                    case (a.length > b.length):
                        return -1;
                    case (a.length < b.length):
                        return 1;
                    default:
                        return 0;
                    }
                });
                //  set title -- first part, if more than one word; otherwise, whole
                //  =========
                _foundTitle = ''+(_doc_title_parts[0].split(/s+/i).length > 1 ? _doc_title_parts[0] : _documentTitle)
                //_foundHTML = '' + Article.articleTitleMarker__start + (_doc_title_parts[0].split(/s+/i).length > 1 ? _doc_title_parts[0] : _documentTitle) + Article.articleTitleMarker__end + _foundHTML;
            })();
            Article.articleTitle = Article.getContent__find__getIsolatedTitleInHTML(_foundHTML);

        }
    }
    
    //  remember
    //  ========
    Article.debugRemember['theTarget'] = _found._targetCandidate.__node;
    Article.debugRemember['firstCandidate'] = _found._firstCandidate.__node;
    //  next
    //  ====

    Article.nextPage__firstFragment__firstPage = _firstFragmentBefore;
    Article.nextPage__firstFragment__lastPage = Article.getContent__nextPage__getFirstFragment(_foundHTML);;
    Article.nextPage__loadedPages = [Article.win.location.href];
    Article.getContent__nextPage__find(Article.win, _found._links);

    //  return

    /*var w = Article.document.createElement('div');
    w.style.cssText = 'display:none;';
    var s = Article.document.createElement('div');
	s.innerHTML+=_foundHTML;*/
    
	var w = Article.document.createElement('div');
    w.style.cssText = 'display:none;';
    var s = Article.document.createElement('div');
    s.style.cssText = 'background-color:white;color:black;text-align:left;font-size:16px;';
    var _style = Article.document.createElement('style');
    _style.appendChild(Article.document.createTextNode(Article.style));
    s.appendChild(_style);
    $(s).append(_foundHTML);


    //w.appendChild(s);
    ///Article.document.body.appendChild(w);

	
    return {html:s, title:_foundTitle, content: s.innerHTML};
};
Article.getContent__findInPage = function (_pageWindow) {
    //  calculations
    //  ============

    var
    _firstCandidate = false,
        _secondCandidate = false,
        _targetCandidate = false;

    var _stuff = Article.getContent__exploreNodeAndGetStuff(_pageWindow.document.body);


    var _processedCandidates = Article.getContent__processCandidates(_stuff._candidates);
    _firstCandidate = _processedCandidates[0];
    _targetCandidate = _firstCandidate;


    
    //  do second?
    switch (true) {
    case ((_firstCandidate._count__containers > 0)):
    case ((_firstCandidate._count__candidates > 0)):
    case ((_firstCandidate._count__pieces > 0)):
    case ((_firstCandidate._count__containers > 25)):
        break;

    default:
        var _processedCandidatesSecond = Article.getContent__processCandidatesSecond(_processedCandidates);
        _secondCandidate = _processedCandidatesSecond[0];


        //  they're the same
        if (_firstCandidate.__node == _secondCandidate.__node) {
            break;
        }



        //  compute again
        //  =============
        _firstCandidate['__points_history_final'] = Article.getContent__computePointsForCandidateThird(_firstCandidate, _firstCandidate);
        _firstCandidate['__points_final'] = _firstCandidate.__points_history_final[0];

        _secondCandidate['__points_history_final'] = Article.getContent__computePointsForCandidateThird(_secondCandidate, _firstCandidate);
        _secondCandidate['__points_final'] = _secondCandidate.__points_history_final[0];




        //  are we selecting _second?
        //  =========================
        switch (true) {
        case ((_secondCandidate.__candidate_details._count__lines_of_65_characters < 20) && (_secondCandidate.__points_final / _firstCandidate.__points_final) > 1):
        case ((_secondCandidate.__candidate_details._count__lines_of_65_characters > 20) && (_secondCandidate.__points_final / _firstCandidate.__points_final) > 0.9):
        case ((_secondCandidate.__candidate_details._count__lines_of_65_characters > 50) && (_secondCandidate.__points_final / _firstCandidate.__points_final) > 0.75):
            _targetCandidate = _secondCandidate;

            break;
        }



        break;
    }


    //  get html
    //  ========
    

    var _html = Article.getContent__buildHTMLForNode(_targetCandidate.__node, 'the-target');
    _html = _html.substr((_html.indexOf('>') + 1))
    _html = _html.substr(0, _html.lastIndexOf('<'));


    _html = _html.replace(/<(blockquote|p|td|li)([^>]*)>(\s*<br \/>)+/gi, '<$1$2>');
    _html = _html.replace(/(<br \/>\s*)+<\/(blockquote|p|td|li)>/gi, '</$2>');
    _html = _html.replace(/(<br \/>\s*)+<(blockquote|h\d|ol|p|table|ul|li)([^>]*)>/gi, '<$2$3>');
    _html = _html.replace(/<\/(blockquote|div|h\d|ol|p|table|ul|li)>(\s*<br \/>)+/gi, '</$1>');
    _html = _html.replace(/(<hr \/>\s*<hr \/>\s*)+/gi, '<hr />');
    _html = _html.replace(/(<br \/>\s*<br \/>\s*)+/gi, '<br /><br />');

    //  return
    //  ======
    return {
        '_html': _html,
        '_links': _stuff._links,
        '_targetCandidate': _targetCandidate,
        '_firstCandidate': _firstCandidate
    };
};


//  get first page fragment
//  =======================

Article.getContent__nextPage__getFirstFragment = function (_html) {
    //  remove all tags
    _html = _html.replace(/<[^>]+?>/gi, '');

    //  normalize spaces
    _html = _html.replace(/\s+/gi, ' ');

    //  return first 1000 characters
    return _html.substr(0, 2000);
};


//  get link parts
//  ==============

//  substr starting with the first slash after //
Article.getURLPath = function (_url) {
    return _url.substr(_url.indexOf('/', (_url.indexOf('//') + 2)));
};

//  substr until the first slash after //
Article.getURLDomain = function (_url) {
    return _url.substr(0, _url.indexOf('/', (_url.indexOf('//') + 2)))
};


//  find
//  ====
Article.getContent__nextPage__find = function (_currentPageWindow, _linksInCurrentPage) {
    //  page id

    var _pageNr = (Article.nextPage__loadedPages.length + 1);

    //  get
    //  ===
    var _possible = [];
    if (_possible.length > 0);
    else {
        _possible = Article.getContent__nextPage__find__possible(_currentPageWindow, _linksInCurrentPage, 0.5);
    }
    //if (_possible.length > 0); else { _possible = Article.getContent__nextPage__find__possible(_currentPageWindow, _linksInCurrentPage, 0.50); }

    //  none
    if (_possible.length > 0);
    else {
        if (Article.debug) {
            Article.log('no next link found');
        }
        return;
    }

    if (Article.debug) {
        Article.log('possible next', _possible);
    }

    //  the one
    //  =======
    var _nextLink = false;

    //  next keyword?
    //  =============
    (function () {
        if (_nextLink) {
            return;
        }

        for (var i = 0, _i = _possible.length; i < _i; i++) {
            for (var j = 0, _j = Article.nextPage__captionKeywords.length; j < _j; j++) {
                if (_possible[i]._caption.indexOf(Article.nextPage__captionKeywords[j]) > -1) {
                    //  length
                    //  ======
                    if (_possible[i]._caption.length > Article.nextPage__captionKeywords[j].length * 2) {
                        continue;
                    }

                    //  not keywords
                    //  ============
                    for (var z = 0, _z = Article.nextPage__captionKeywords__not.length; z < _z; z++) {
                        if (_possible[i]._caption.indexOf(Article.nextPage__captionKeywords__not[z]) > -1) {
                            _nextLink = false;
                            return;
                        }
                    }

                    //  got it
                    //  ======
                    _nextLink = _possible[i];
                    return;
                }
            }
        }
    })();

    //  caption matched page number
    //  ===========================
    (function () {
        if (_nextLink) {
            return;
        }

        for (var i = 0, _i = _possible.length; i < _i; i++) {
            if (_possible[i]._caption == ('' + _pageNr)) {
                _nextLink = _possible[i];
                return;
            }
        }
    })();

    //  next keyword in title
    //  =====================
    (function () {
        if (_nextLink) {
            return;
        }

        for (var i = 0, _i = _possible.length; i < _i; i++) {
            //  sanity
            if (_possible[i]._title > '');
            else {
                continue;
            }
            if (Article.measureText__getTextLength(_possible[i]._caption) <= 2);
            else {
                continue;
            }

            for (var j = 0, _j = Article.nextPage__captionKeywords.length; j < _j; j++) {
                if (_possible[i]._title.indexOf(Article.nextPage__captionKeywords[j]) > -1) {
                    //  length
                    //  ======
                    if (_possible[i]._title.length > Article.nextPage__captionKeywords[j].length * 2) {
                        continue;
                    }

                    //  not keywords
                    //  ============
                    for (var z = 0, _z = Article.nextPage__captionKeywords__not.length; z < _z; z++) {
                        if (_possible[i]._title.indexOf(Article.nextPage__captionKeywords__not[z]) > -1) {
                            _nextLink = false;
                            return;
                        }
                    }

                    //  got it
                    //  ======
                    _nextLink = _possible[i];
                    return;
                }
            }
        }
    })();

    //  return?
    //  =======
    if (_nextLink);
    else {
        return;
    }

    //  mark
    //  ====
    Article.debugPrint('NextPage', 'true');

    if (Article.debug) {
        Article.debugOutline(_nextLink._node, 'target', 'next-page');
        Article.log('NextPage Link', _nextLink, _nextLink._node);
    }

    //  process page
    //  ============
    Article.getContent__nextPage__loadToFrame(_pageNr, _nextLink._href);
    Article.nextPage__loadedPages.push(_nextLink._href);
};


//  find with similarity
//  ====================
Article.getContent__nextPage__find__possible = function (_currentPageWindow, _linksInCurrentPage, _distanceFactor) {
    var
    _mainPageHref = Article.win.location.href,
        _mainPageDomain = Article.getURLDomain(_mainPageHref),
        _mainPagePath = Article.getURLPath(_mainPageHref);

    var _links = $.map(
        _linksInCurrentPage,
        function (_element, _index) {
            var
            _href = _element.__node.href,
                _path = Article.getURLPath(_href),
                _title = (_element.__node.title > '' ? _element.__node.title.toLowerCase() : ''),
                _caption = _element.__node.innerHTML.replace(/<[^>]+?>/gi, '').replace(/\&[^\&\s;]{1,10};/gi, '').replace(/\s+/gi, ' ').replace(/^ /, '').replace(/ $/, '').toLowerCase(),
                _distance = Article.levenshteinDistance(_mainPagePath, _path);

            var _caption2 = '';
            for (var i = 0, _i = _caption.length, _code = 0; i < _i; i++) {
                _code = _caption.charCodeAt(i);
                _caption2 += (_code > 127 ? ('&#' + _code + ';') : _caption.charAt(i));
            }
            _caption = _caption2;

            switch (true) {
            case (!(_href > '')):
            case (_mainPageHref.length > _href.length):
            case (_mainPageDomain != Article.getURLDomain(_href)):
            case (_href.substr(_mainPageHref.length).substr(0, 1) == '#'):
            case (_distance > Math.ceil(_distanceFactor * _path.length)):
                return null;

            default:
                //  skip if already loaded as next page
                for (var i = 0, _i = Article.nextPage__loadedPages.length; i < _i; i++) {
                    if (Article.nextPage__loadedPages[i] == _href) {
                        return null;
                    }
                }

                //  return
                return {
                    '_node': _element.__node,
                    '_href': _href,
                    '_title': _title,
                    '_caption': _caption,
                    '_distance': _distance
                };
            }
        }
    );

    //  sort -- the less points, the closer to position 0
    //  ====
    _links.sort(function (a, b) {
        switch (true) {
        case (a._distance < b._distance):
            return -1;
        case (a._distance > b._distance):
            return 1;
        default:
            return 0;
        }
    });


    //  return
    return _links;
};




//  load to frame
//  =============
Article.getContent__nextPage__loadToFrame = function (_pageNr, _nextPageURL) {
    //  do ajax
    //  =======
    $.ajax({
        'url': _nextPageURL,

        'type': 'GET',
        'dataType': 'html',
        'async': true,
        'timeout': (10 * 1000),

        //'headers': { 'Referrer': _nextPageURL },

        'success': function (_response, _textStatus, _xhr) {
            Article.getContent__nextPage__ajaxComplete(_pageNr, _response, _textStatus, _xhr);
        },
        'error': function (_xhr, _textStatus, _error) {
            Article.getContent__nextPage__ajaxError(_pageNr, _xhr, _textStatus, _error);
        }
    });
};


//  ajax calbacks
//  =============
Article.getContent__nextPage__ajaxError = function (_pageNr, _xhr, _textStatus, _error) {};

Article.getContent__nextPage__ajaxComplete = function (_pageNr, _response, _textStatus, _xhr) {
    //  valid?
    //  ======
    if (_response > '');
    else {
        return;
    }

    //  script
    //  ======
    var _script = '' + '<script type="text/javascript">' + ' function __this_page_loaded()' + ' {' + '     window.setTimeout(' + '         function () {' + (Article.component ? 'window.parent.' : 'window.parent.parent.') + 'Article.getContent__nextPage__loadedInFrame("' + _pageNr + '", window); }, ' + '         250' + '     );' + ' } '

    + ' if (document.readyState); else { __this_page_loaded(); } '

    + ' function __this_page_loaded_ready(delayedNrTimes)' + ' {' + '     if (document.readyState != "complete" && delayedNrTimes < 30)' + '         { setTimeout(function () { __this_page_loaded_ready(delayedNrTimes+1); }, 100); return; }'

    + '     __this_page_loaded();' + ' }'

    + ' __this_page_loaded_ready(0);' + '</script>';

    //  get html
    //  ========
    var _html = _response;

    //  normalize
    //  =========
    _html = _html.replace(/<\s+/gi, '<');
    _html = _html.replace(/\s+>/gi, '>');
    _html = _html.replace(/\s+\/>/gi, '/>');

    //  remove
    //  ======
    _html = _html.replace(/<script[^>]*?>([\s\S]*?)<\/script>/gi, '');
    _html = _html.replace(/<script[^>]*?\/>/gi, '');
    _html = _html.replace(/<noscript[^>]*?>([\s\S]*?)<\/noscript>/gi, '');

    //  add load handler
    //  ================
    _html = _html.replace(/<\/body/i, _script + '</body');

	
    //  append frame
    //  ============
   try{
		/*var page=document.createElement('iframe');
		page.innerHTML='' + '<iframe' + ' id="nextPageFrame__' + _pageNr + '"' + ' scrolling="no" frameborder="0"' + '></iframe>';
		document.body.appendChild(page);
		
		var _doc = page.contentWindow.document;
		//alert(_html);
		_doc.open();
		_doc.write(_html);
		_doc.close();*/
		
	    Article.$nextPages.append('' + '<iframe' + ' id="nextPageFrame__' + _pageNr + '"' + ' scrolling="no" frameborder="0"' + '></iframe>');
		var _doc = $('#nextPageFrame__' + _pageNr).contents().get(0);


		_doc.open();
		_doc.write(_html);
		_doc.close();

		}
	catch (exp){}
};


//  loaded in frame
//  ===============
Article.getContent__nextPage__loadedInFrame = function (_pageNr, _pageWindow) {
    //  find
    //  ====
    var
    _found = Article.getContent__findInPage(_pageWindow),
        _foundHTML = _found._html,
        _removeTitleRegex = new RegExp(Article.articleTitleMarker__start + '(.*?)' + Article.articleTitleMarker__end, 'i');

    //  get first fragment
    //  ==================
    var _firstFragment = Article.getContent__nextPage__getFirstFragment(_foundHTML);

    //  gets first 2000 characters
    //  diff set at 100 -- 0.05
    switch (true) {
    case (Article.levenshteinDistance(_firstFragment, Article.nextPage__firstFragment__firstPage) < 100):
    case (Article.levenshteinDistance(_firstFragment, Article.nextPage__firstFragment__lastPage) < 100):

        //  mark
        Article.debugPrint('NextPage', 'false');

        //  mark again
        if (Article.debug) {
            $('#debugOutput__value__NextPage').html('false');
        }

        //  pop page
        Article.nextPage__loadedPages.pop();

        //  break
        return false;

    default:
        //  add to first fragemnts
        Article.nextPage__firstFragment__lastPage = _firstFragment;
        break;
    }

    //  remove title -- do it twice
    //  ============

    //  once with document title
    _foundHTML = Article.getContent__find__isolateTitleInHTML(_foundHTML, (Article.document.title > '' ? Article.document.title : ''));
    _foundHTML = _foundHTML.replace(_removeTitleRegex, '');

    //  once with article title
    _foundHTML = Article.getContent__find__isolateTitleInHTML(_foundHTML, Article.articleTitle);
    _foundHTML = _foundHTML.replace(_removeTitleRegex, '');


    
    //  next
    //  ====
    Article.getContent__nextPage__find(_pageWindow, _found._links);
};
