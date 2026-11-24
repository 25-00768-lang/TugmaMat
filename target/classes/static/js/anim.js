/* TugmaMat animations: scroll-reveal for every box, staggered, plus counting numbers. */
(function () {
  var BOXES = '.feature, .statrow dl, .filters, .card, .panel, .table, .empty, .error, .detail > div, .demo';
  var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function countUp(el) {
    var m = /^(\d+)(.*)$/.exec(el.textContent.trim());
    if (!m || reduce) return;
    var target = parseInt(m[1], 10), suffix = m[2], start = null, dur = 1100;
    if (target === 0) return;
    el.textContent = '0' + suffix;
    function step(t) {
      if (start === null) start = t;
      var p = Math.min((t - start) / dur, 1);
      var eased = 1 - Math.pow(1 - p, 3);
      el.textContent = Math.round(target * eased) + suffix;
      if (p < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
  }

  function reveal(el, i) {
    el.style.setProperty('--d', Math.min(i, 8) * 90 + 'ms');
    var kids = el.querySelectorAll('tbody tr, .checks li');
    for (var k = 0; k < kids.length; k++) kids[k].style.setProperty('--r', Math.min(k, 14));
    el.classList.add('in');
    var nums = el.querySelectorAll('.statrow dd, .score strong');
    for (var n = 0; n < nums.length; n++) countUp(nums[n]);
  }

  function init() {
    var els = Array.prototype.slice.call(document.querySelectorAll(BOXES));
    if (!('IntersectionObserver' in window)) { els.forEach(function (el, i) { reveal(el, i); }); return; }
    var io = new IntersectionObserver(function (entries) {
      var seen = 0;
      entries.forEach(function (e) {
        if (!e.isIntersecting) return;
        reveal(e.target, seen++);
        io.unobserve(e.target);
      });
    }, { threshold: 0.05, rootMargin: '0px 0px -30px 0px' });
    els.forEach(function (el) { io.observe(el); });
    // safety net: never leave a box hidden
    setTimeout(function () { els.forEach(function (el) { if (!el.classList.contains('in')) reveal(el, 0); }); }, 6000);
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();
