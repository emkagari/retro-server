// patch.ts
import { existsSync, readdirSync, readFileSync as readFileSync2, writeFileSync } from "node:fs";
import { join } from "node:path";

// ../../../experimentation/packages/swf/src/script.ts
import { readFileSync } from "node:fs";

// ../../../experimentation/packages/swf/src/avm1.ts
var OP = {
  End: 0,
  Play: 6,
  Stop: 7,
  Add: 10,
  Subtract: 11,
  Multiply: 12,
  Pop: 23,
  ToInteger: 24,
  GetVariable: 28,
  SetVariable: 29,
  GetProperty: 34,
  RandomNumber: 48,
  CallFunction: 61,
  Modulo: 63,
  NewObject: 64,
  InitArray: 66,
  InitObject: 67,
  Add2: 71,
  Equals2: 73,
  GetMember: 78,
  SetMember: 79,
  CallMethod: 82,
  GotoFrame: 129,
  ConstantPool: 136,
  Push: 150,
  GotoFrame2: 159
};
var Avm1Error = class extends Error {
};
function parseActions(bc) {
  const out2 = [];
  let p = 0;
  while (p < bc.length) {
    const op = bc[p];
    if (op === OP.End) break;
    if (op < 128) {
      out2.push({ op, body: Buffer.alloc(0) });
      p += 1;
      continue;
    }
    if (p + 3 > bc.length) throw new Avm1Error(`truncated action 0x${op.toString(16)} at ${p}`);
    const len = bc.readUInt16LE(p + 1);
    if (p + 3 + len > bc.length) throw new Avm1Error(`truncated action 0x${op.toString(16)} at ${p}`);
    out2.push({ op, body: bc.subarray(p + 3, p + 3 + len) });
    p += 3 + len;
  }
  return out2;
}
function writeActions(actions) {
  const parts = [];
  for (const a of actions) {
    if (a.op < 128) {
      parts.push(Buffer.of(a.op));
      continue;
    }
    if (a.body.length > 65535) throw new Avm1Error(`action 0x${a.op.toString(16)} too long: ${a.body.length}`);
    const h = Buffer.alloc(3);
    h[0] = a.op;
    h.writeUInt16LE(a.body.length, 1);
    parts.push(h, a.body);
  }
  parts.push(Buffer.of(OP.End));
  return Buffer.concat(parts);
}
function readConstantPool(body, utf8) {
  const n = body.readUInt16LE(0);
  const pool = [];
  let q = 2;
  for (let i = 0; i < n; i++) {
    const end = body.indexOf(0, q);
    if (end < 0) break;
    pool.push(body.subarray(q, end).toString(utf8 ? "utf8" : "latin1"));
    q = end + 1;
  }
  return pool;
}
function readSwappedDouble(b, p) {
  const t = Buffer.alloc(8);
  b.copy(t, 4, p, p + 4);
  b.copy(t, 0, p + 4, p + 8);
  return t.readDoubleLE(0);
}
function writeSwappedDouble(v) {
  const t = Buffer.alloc(8);
  t.writeDoubleLE(v, 0);
  return Buffer.concat([t.subarray(4, 8), t.subarray(0, 4)]);
}
function readPush(body, pool, utf8) {
  const vals = [];
  let p = 0;
  while (p < body.length) {
    const type = body[p];
    p += 1;
    switch (type) {
      case 0: {
        const end = body.indexOf(0, p);
        if (end < 0) throw new Avm1Error("unterminated string in Push");
        vals.push(body.subarray(p, end).toString(utf8 ? "utf8" : "latin1"));
        p = end + 1;
        break;
      }
      case 1:
        vals.push(body.readFloatLE(p));
        p += 4;
        break;
      case 2:
        vals.push(null);
        break;
      case 3:
        vals.push(void 0);
        break;
      case 4:
        vals.push({ register: body[p] });
        p += 1;
        break;
      case 5:
        vals.push(body[p] !== 0);
        p += 1;
        break;
      case 6:
        vals.push(readSwappedDouble(body, p));
        p += 8;
        break;
      case 7:
        vals.push(body.readInt32LE(p));
        p += 4;
        break;
      case 8:
        vals.push(pool[body[p]]);
        p += 1;
        break;
      case 9:
        vals.push(pool[body.readUInt16LE(p)]);
        p += 2;
        break;
      default:
        throw new Avm1Error(`unknown Push type ${type}`);
    }
  }
  return vals;
}
function encodePushValue(v) {
  if (v === null) return Buffer.of(2);
  if (v === void 0) return Buffer.of(3);
  if (typeof v === "boolean") return Buffer.of(5, v ? 1 : 0);
  if (typeof v === "string") {
    if (v.includes("\0")) throw new Avm1Error("NUL in a pushed string");
    return Buffer.concat([Buffer.of(0), Buffer.from(v, "utf8"), Buffer.of(0)]);
  }
  if (typeof v === "number") {
    if (Number.isInteger(v) && v >= -2147483648 && v <= 2147483647 && !Object.is(v, -0)) {
      const b = Buffer.alloc(5);
      b[0] = 7;
      b.writeInt32LE(v, 1);
      return b;
    }
    return Buffer.concat([Buffer.of(6), writeSwappedDouble(v)]);
  }
  return Buffer.of(4, v.register);
}
function pushActions(values) {
  const out2 = [];
  let cur = [];
  let size = 0;
  for (const v of values) {
    const b = encodePushValue(v);
    if (size + b.length > 65535 && cur.length) {
      out2.push({ op: OP.Push, body: Buffer.concat(cur) });
      cur = [];
      size = 0;
    }
    cur.push(b);
    size += b.length;
  }
  if (cur.length) out2.push({ op: OP.Push, body: Buffer.concat(cur) });
  return out2;
}

// ../../../experimentation/packages/swf/src/container.ts
import { deflateSync, inflateSync } from "node:zlib";
var SwfError = class extends Error {
};
function parseSwf(raw) {
  const sig = raw.subarray(0, 3).toString("latin1");
  if (sig !== "FWS" && sig !== "CWS") throw new SwfError(`not a SWF (or unsupported compression): ${sig}`);
  const compressed = sig === "CWS";
  const body = compressed ? inflateSync(raw.subarray(8)) : raw.subarray(8);
  const nbits = body[0] >> 3;
  const headerLen = Math.ceil((5 + 4 * nbits) / 8) + 4;
  const tags = [];
  let off = headerLen;
  while (off + 2 <= body.length) {
    const th = body.readUInt16LE(off);
    off += 2;
    const code = th >> 6;
    let len = th & 63;
    const long = len === 63;
    if (long) {
      len = body.readUInt32LE(off);
      off += 4;
    }
    if (off + len > body.length) throw new SwfError(`truncated tag ${code} at ${off}`);
    tags.push({ code, data: Buffer.from(body.subarray(off, off + len)), long });
    off += len;
    if (code === 0) break;
  }
  return { compressed, version: raw[3], frameHeader: Buffer.from(body.subarray(0, headerLen)), tags };
}
function writeSwf(swf) {
  const parts = [swf.frameHeader];
  for (const t of swf.tags) {
    const long = t.long || t.data.length >= 63;
    const h = Buffer.alloc(long ? 6 : 2);
    h.writeUInt16LE(t.code << 6 | (long ? 63 : t.data.length), 0);
    if (long) h.writeUInt32LE(t.data.length, 2);
    parts.push(h, t.data);
  }
  const body = Buffer.concat(parts);
  const head = Buffer.alloc(8);
  head.write(swf.compressed ? "CWS" : "FWS", 0, "latin1");
  head[3] = swf.version;
  head.writeUInt32LE(body.length + 8, 4);
  return Buffer.concat([head, swf.compressed ? deflateSync(body, { level: 9 }) : body]);
}
var TAG = {
  End: 0,
  ShowFrame: 1,
  DefineSound: 14,
  DoAction: 12,
  DefineSprite: 39,
  PlaceObject2: 26,
  ExportAssets: 56,
  DoInitAction: 59
};

// ../../../experimentation/packages/swf/src/script.ts
var ScriptError = class extends Error {
};
var lit = (v) => ({ k: "lit", v });
var opaque = (text) => ({ k: "opaque", text });
var show = (e) => !e ? "?" : e.k === "lit" ? JSON.stringify(e.v) ?? "undefined" : e.k === "ref" ? e.path.join(".") : e.text;
function decompile(actions, utf8) {
  const out2 = [];
  let pool = [];
  let stack = [];
  let start = 0;
  const pop = () => stack.pop() ?? opaque("<empty stack>");
  const count = () => {
    const n = pop();
    if (n.k !== "lit" || typeof n.v !== "number") throw new ScriptError(`expected a count, got ${show(n)}`);
    return n.v;
  };
  const key = (e) => e.k === "lit" && (typeof e.v === "string" || typeof e.v === "number") ? e.v : null;
  const close = (i, target, value) => {
    const s = { start, end: i + 1, target };
    if (value?.k === "lit") s.value = value.v;
    else if (value) s.opaque = show(value);
    out2.push(s);
    start = i + 1;
  };
  actions.forEach((a, i) => {
    switch (a.op) {
      case OP.ConstantPool:
        pool = readConstantPool(a.body, utf8);
        break;
      case OP.Push:
        for (const v of readPush(a.body, pool, utf8)) {
          stack.push(typeof v === "object" && v !== null ? opaque(`register ${v.register}`) : lit(v));
        }
        break;
      case OP.GetVariable: {
        const n = key(pop());
        stack.push(n === null ? opaque("<dynamic variable>") : { k: "ref", path: [n] });
        break;
      }
      case OP.GetMember: {
        const name = pop(), obj = pop();
        const n = key(name);
        stack.push(obj.k === "ref" && n !== null ? { k: "ref", path: [...obj.path, n] } : opaque(`${show(obj)}[${show(name)}]`));
        break;
      }
      case OP.SetVariable: {
        const value = pop(), name = key(pop());
        close(i, name === null ? null : [name], value);
        break;
      }
      case OP.SetMember: {
        const value = pop(), name = pop(), obj = pop();
        const n = key(name);
        close(i, obj.k === "ref" && n !== null ? [...obj.path, n] : null, value);
        break;
      }
      case OP.InitObject: {
        const n = count();
        const pairs = [];
        for (let j = 0; j < n; j++) {
          const v = pop();
          pairs.push([key(pop()), v]);
        }
        const o = {};
        let ok = true;
        for (const [name, v] of pairs.reverse()) {
          if (v.k !== "lit" || name === null) ok = false;
          else o[String(name)] = v.v;
        }
        stack.push(ok ? lit(o) : opaque("{\u2026}"));
        break;
      }
      case OP.InitArray: {
        const n = count();
        const arr = [];
        let ok = true;
        for (let j = 0; j < n; j++) {
          const v = pop();
          if (v.k !== "lit") ok = false;
          else arr.push(v.v);
        }
        stack.push(ok ? lit(arr) : opaque("[\u2026]"));
        break;
      }
      case OP.NewObject: {
        const name = pop(), n = count();
        for (let j = 0; j < n; j++) pop();
        stack.push(opaque(`new ${show(name)}(\u2026)`));
        break;
      }
      case OP.CallMethod: {
        const name = pop(), obj = pop(), n = count();
        for (let j = 0; j < n; j++) pop();
        stack.push(opaque(`${show(obj)}.${show(name)}(\u2026)`));
        break;
      }
      case OP.CallFunction: {
        const name = pop(), n = count();
        for (let j = 0; j < n; j++) pop();
        stack.push(opaque(`${show(name)}(\u2026)`));
        break;
      }
      case OP.Add:
      case OP.Add2: {
        const b = pop(), x = pop();
        const prim = (e) => e.k === "lit" && (typeof e.v === "string" || typeof e.v === "number") ? e.v : null;
        const l = prim(x), r = prim(b);
        stack.push(l !== null && r !== null ? lit(typeof l === "string" || typeof r === "string" ? `${l}${r}` : l + r) : opaque(`${show(x)} + ${show(b)}`));
        break;
      }
      case OP.Pop:
        pop();
        break;
      default:
        throw new ScriptError(`unsupported action 0x${a.op.toString(16)} at ${i}`);
    }
    if (stack.length === 0 && a.op !== OP.Push && start <= i) close(i, null);
  });
  if (start < actions.length) {
    out2.push({ start, end: actions.length, target: null, opaque: `<${stack.length} value(s) left>` });
  }
  return out2;
}
function compileAssign(path, value) {
  if (path.length === 0) throw new ScriptError("empty assignment target");
  const out2 = [];
  let pending = [];
  const flush = () => {
    if (pending.length) {
      out2.push(...pushActions(pending));
      pending = [];
    }
  };
  const op = (code) => {
    flush();
    out2.push({ op: code, body: Buffer.alloc(0) });
  };
  const emit = (v) => {
    if (Array.isArray(v)) {
      for (let j = v.length - 1; j >= 0; j--) emit(v[j]);
      pending.push(v.length);
      op(OP.InitArray);
    } else if (v !== null && typeof v === "object") {
      const entries = Object.entries(v);
      for (const [k, x] of entries) {
        pending.push(k);
        emit(x);
      }
      pending.push(entries.length);
      op(OP.InitObject);
    } else {
      pending.push(v);
    }
  };
  if (path.length === 1) {
    pending.push(path[0]);
    emit(value);
    op(OP.SetVariable);
    return out2;
  }
  pending.push(path[0]);
  op(OP.GetVariable);
  for (const k of path.slice(1, -1)) {
    pending.push(k);
    op(OP.GetMember);
  }
  pending.push(path.at(-1));
  emit(value);
  op(OP.SetMember);
  return out2;
}
var pathKey = (p) => p.map(String).join("\0");
var DataScript = class _DataScript {
  constructor(raw) {
    this.raw = raw;
    this.swf = parseSwf(raw);
    const utf8 = this.swf.version >= 6;
    this.swf.tags.forEach((t, tag) => {
      if (t.code !== TAG.DoAction) return;
      const actions = parseActions(t.data);
      const block = {
        tag,
        actions,
        statements: decompile(actions, utf8),
        replaced: /* @__PURE__ */ new Map(),
        appended: [],
        dirty: false
      };
      this.blocks.push(block);
      block.statements.forEach((s, i) => {
        if (s.target) this.index.set(pathKey(s.target), { block, index: i, path: s.target, value: s.value, opaque: s.opaque });
      });
    });
    if (this.blocks.length === 0) throw new ScriptError("no frame script in this SWF");
  }
  raw;
  swf;
  blocks = [];
  /** Last assignment of each path (execution order: the last one wins). */
  index = /* @__PURE__ */ new Map();
  static fromFile(path) {
    return new _DataScript(readFileSync(path));
  }
  static fromBuffer(buf) {
    return new _DataScript(buf);
  }
  /** Value assigned to `path`, `undefined` if never assigned or not a literal. */
  get(path) {
    return this.index.get(pathKey(path))?.value;
  }
  has(path) {
    return this.index.has(pathKey(path));
  }
  /** Every `prefix[k] = value` assignment, in file order (literals only). */
  table(prefix) {
    const out2 = /* @__PURE__ */ new Map();
    const head = pathKey(prefix) + "\0";
    for (const [k, e] of this.index) {
      if (e.path.length !== prefix.length + 1 || !k.startsWith(head) || e.value === void 0) continue;
      out2.set(String(e.path.at(-1)), e.value);
    }
    return out2;
  }
  /** Every assignment, for inspection. */
  assignments() {
    return [...this.index.values()].map((e) => ({ path: e.path, value: e.value, opaque: e.opaque }));
  }
  /**
   * Sets `path = value`: replaces the statement assigning it, else appends
   * one to the LAST frame script (like the client's own files do).
   */
  set(path, value) {
    const code = compileAssign(path, value);
    const k = pathKey(path);
    const at = this.index.get(k);
    if (at && at.index !== null) {
      at.block.replaced.set(at.index, code);
      at.block.dirty = true;
      this.index.set(k, { ...at, value, opaque: void 0 });
      return;
    }
    if (at && at.index === null) {
      const e = at.block.appended.find((c) => c.key === k);
      if (e) e.code = code;
      this.index.set(k, { ...at, value });
      return;
    }
    const block = this.blocks.at(-1);
    block.appended.push({ key: k, code });
    block.dirty = true;
    this.index.set(k, { block, index: null, path, value });
  }
  get dirty() {
    return this.blocks.some((b) => b.dirty);
  }
  toBuffer() {
    if (!this.dirty) return this.raw;
    const tags = this.swf.tags.map((t) => ({ ...t }));
    for (const b of this.blocks) {
      if (!b.dirty) continue;
      const actions = [];
      b.statements.forEach((s, i) => {
        actions.push(...b.replaced.get(i) ?? b.actions.slice(s.start, s.end));
      });
      for (const c of b.appended) actions.push(...c.code);
      tags[b.tag] = { ...tags[b.tag], data: writeActions(actions) };
    }
    return writeSwf({ ...this.swf, tags });
  }
};

// ../../../experimentation/packages/dofus-client/src/literal.ts
function parseLiteral(src) {
  let p = 0;
  const ws = () => {
    while (p < src.length && /\s/.test(src[p])) p++;
  };
  const fail = (what) => {
    throw new Error(`invalid literal at ${p}: ${what} in ${src.slice(0, 80)}`);
  };
  const value = () => {
    ws();
    const ch = src[p];
    if (ch === "{") {
      p++;
      const o = {};
      ws();
      if (src[p] === "}") {
        p++;
        return o;
      }
      for (; ; ) {
        ws();
        let k;
        if (src[p] === '"') k = String(value());
        else {
          const m2 = /^[A-Za-z0-9_$]+/.exec(src.slice(p));
          if (!m2) fail("key");
          k = m2[0];
          p += k.length;
        }
        ws();
        if (src[p] !== ":") fail("':'");
        p++;
        o[k] = value();
        ws();
        if (src[p] === ",") {
          p++;
          continue;
        }
        if (src[p] === "}") {
          p++;
          return o;
        }
        fail("',' or '}'");
      }
    }
    if (ch === "[") {
      p++;
      const a = [];
      ws();
      if (src[p] === "]") {
        p++;
        return a;
      }
      for (; ; ) {
        a.push(value());
        ws();
        if (src[p] === ",") {
          p++;
          continue;
        }
        if (src[p] === "]") {
          p++;
          return a;
        }
        fail("',' or ']'");
      }
    }
    if (ch === '"') {
      p++;
      let s = "";
      while (p < src.length && src[p] !== '"') {
        if (src[p] === "\\") {
          p++;
          const e = src[p];
          s += e === "n" ? "\n" : e === "t" ? "	" : e;
        } else s += src[p];
        p++;
      }
      if (src[p] !== '"') fail("closing quote");
      p++;
      return s;
    }
    const m = /^(true|false|null|undefined|-?\d+(?:\.\d+)?(?:e[+-]?\d+)?)/i.exec(src.slice(p));
    if (!m) return fail("value");
    p += m[0].length;
    const t = m[0];
    return t === "true" ? true : t === "false" ? false : t === "null" ? null : t === "undefined" ? void 0 : Number(t);
  };
  const v = value();
  ws();
  if (p !== src.length) fail("trailing text");
  return v;
}

// ../../../experimentation/packages/dofus-client/src/lang.ts
var LangError = class extends Error {
};
var isObj = (v) => !!v && typeof v === "object" && !Array.isArray(v);
var areaValue = (a) => {
  const o = typeof a === "string" ? parseLiteral(a) : a;
  return typeof a === "string" ? o : { n: String(o.n ?? ""), sua: Number(o.sua ?? 0) };
};
var subAreaValue = (a) => {
  const o = typeof a === "string" ? parseLiteral(a) : a;
  if (typeof a === "string") return o;
  return {
    n: String(o.n ?? ""),
    a: Number(o.a ?? 0),
    m: Array.isArray(o.m) ? o.m : [],
    v: Array.isArray(o.v) ? o.v : []
  };
};
function applyMaps(s, ps) {
  for (const [key, v] of Object.entries(ps.entries)) {
    if (!v) continue;
    const e = v;
    const cur = s.get(["MA", "m", Number(key)]);
    s.set(["MA", "m", Number(key)], { ...isObj(cur) ? cur : {}, x: e.x, y: e.y, sa: e.sa });
  }
  const merged = (path, v) => {
    const cur = s.get(path);
    return isObj(cur) ? { ...cur, ...v } : v;
  };
  for (const [key, a] of Object.entries(ps.areas ?? {})) s.set(["MA", "a", Number(key)], merged(["MA", "a", Number(key)], areaValue(a)));
  for (const [key, a] of Object.entries(ps.subAreas ?? {})) s.set(["MA", "sa", Number(key)], merged(["MA", "sa", Number(key)], subAreaValue(a)));
}
function applyHints(s, ps) {
  const hi = s.get(["HI"]);
  if (!Array.isArray(hi)) throw new LangError("HI array not found in the lang file");
  let list = [...hi];
  for (const [key, v] of Object.entries(ps.entries)) {
    const m = Number(key);
    const at = list.findIndex((x) => isObj(x) && x.m === m);
    if (v === null) {
      if (at >= 0) list = list.filter((_, i) => i !== at);
      continue;
    }
    const h = v;
    const obj = { n: h.n, c: h.c, g: h.g, m };
    if (at >= 0) list[at] = obj;
    else list.push(obj);
  }
  s.set(["HI"], list);
}

// patch.ts
var [out, origDir, customDir, ...langs] = process.argv.slice(2);
var ids = (process.env.SERVERS ?? "2").split(",").map((x) => Number(x.trim())).filter(Number.isFinite);
var index = DataScript.fromFile(join(out, "versions.official.swf"));
var stateDir = process.env.STATE_DIR ?? null;
var editorState = (lang) => {
  const f = stateDir ? join(stateDir, `lang-maps.${lang}.json`) : null;
  try {
    return f && existsSync(f) ? JSON.parse(readFileSync2(f, "utf8")) : null;
  } catch {
    return null;
  }
};
function ours(pack, lang) {
  const re = new RegExp(`^${pack}_${lang}_(\\d+)\\.swf$`);
  let best = null;
  for (const dir of [join(customDir, "swf"), join(origDir, "swf")]) {
    if (!existsSync(dir)) continue;
    for (const f of readdirSync(dir)) {
      const n = Number(re.exec(f)?.[1] ?? NaN);
      if (Number.isFinite(n) && (!best || n > best.n)) best = { path: join(dir, f), n };
    }
  }
  return best?.path ?? null;
}
var isObj2 = (v) => !!v && typeof v === "object" && !Array.isArray(v);
function mergeTable(script, from, path) {
  const mine = from.table(path), theirs = script.table(path);
  let added = 0, changed = 0;
  for (const [id, body] of mine) {
    const prev = theirs.get(id);
    const next = isObj2(prev) && isObj2(body) ? { ...prev, ...body } : body;
    if (prev !== void 0 && JSON.stringify(sorted(prev)) === JSON.stringify(sorted(next))) continue;
    script.set([...path, Number(id)], next);
    if (prev === void 0) added++;
    else changed++;
  }
  return `${path.join(".")} +${added} ~${changed}`;
}
var sorted = (v) => isObj2(v) ? Object.fromEntries(Object.keys(v).sort().map((k) => [k, sorted(v[k])])) : v;
function publish(pack, lang, txt, patch, versionOffset = 1) {
  const m = new RegExp(`(?:^|\\|)(?:&f=)?${pack},([a-z]*),(\\d+)`).exec(txt);
  if (!m) {
    console.log(`  ${lang}: no ${pack} entry in versions_${lang}.txt`);
    return txt;
  }
  const official = Number(m[2]), next = official + versionOffset;
  const script = DataScript.fromFile(join(out, "swf", `${pack}_${lang}_${official}.swf`));
  const what = patch(script);
  if (script.has(["VERSION"])) script.set(["VERSION"], next);
  writeFileSync(join(out, "swf", `${pack}_${lang}_${next}.swf`), script.toBuffer());
  if (index.has(["VERSIONS", lang, pack])) index.set(["VERSIONS", lang, pack], next);
  console.log(`  ${lang}: ${pack} ${official} \u2192 ${next}, ${what}`);
  return txt.replace(new RegExp(`((?:^|\\|)(?:&f=)?${pack},[a-z]*,)\\d+`), `$1${next}`);
}
for (const lang of langs) {
  let txt = readFileSync2(join(out, `versions_${lang}.official.txt`), "utf8");
  txt = publish("servers", lang, txt, (script) => {
    const template = [...script.table(["SR"]).values()][0];
    const added = [];
    for (const id of ids) {
      let body;
      for (const l of [lang, "fr", "en"]) {
        const file = ours("servers", l);
        const b = file ? DataScript.fromFile(file).get(["SR", id]) : void 0;
        if (isObj2(b)) {
          body = b;
          break;
        }
      }
      if (!body) {
        console.log(`  ${lang}: server ${id} found in no 1.43 servers file`);
        continue;
      }
      const entry = { ...body };
      if (template && "i" in template && !("i" in entry)) entry.i = 0;
      script.set(["SR", id], entry);
      added.push(`${id} (${String(entry.n ?? "?")})`);
    }
    return `added ${added.join(", ") || "nothing"}`;
  });
  const state = editorState(lang);
  const edits = (n) => n ? `, editor edits ${n}` : "";
  const maps = ours("maps", lang);
  if (maps) {
    const from = DataScript.fromFile(maps);
    txt = publish("maps", lang, txt, (script) => {
      const merged = ["m", "a", "sa"].map((t) => mergeTable(script, from, ["MA", t])).join(", ");
      if (state?.maps) applyMaps(script, state.maps);
      return `${merged} (from ${maps.split("/").slice(-1)[0]})${edits(Object.keys(state?.maps?.entries ?? {}).length)}`;
    });
  }
  const hints = ours("hints", lang);
  if (hints) {
    const list = DataScript.fromFile(hints).get(["HI"]);
    txt = publish("hints", lang, txt, (script) => {
      if (!Array.isArray(list)) return "no HI list in the 1.43 file";
      const officialHints = script.get(["HI"]);
      if (!Array.isArray(officialHints)) throw new LangError("HI array not found in the lang file");
      const officialDungeonHints = new Map(
        officialHints.filter((hint) => isObj2(hint) && hint.c === 6).map((hint) => [hint.m, hint])
      );
      if (officialDungeonHints.size === 0) throw new LangError("official dungeon hints not found in the lang file");
      script.set(["HI"], list);
      if (state?.hints) applyHints(script, state.hints);
      const mergedList = script.get(["HI"]);
      if (!Array.isArray(mergedList)) throw new LangError("patched HI array not found in the lang file");
      const mergedHints = mergedList.map((hint) => {
        if (!isObj2(hint) || hint.c !== 6) return hint;
        return officialDungeonHints.get(hint.m) ?? hint;
      });
      const includedDungeonMaps = new Set(
        mergedHints.filter((hint) => isObj2(hint) && hint.c === 6).map((hint) => hint.m)
      );
      for (const [map, hint] of officialDungeonHints) {
        if (!includedDungeonMaps.has(map)) mergedHints.push(hint);
      }
      script.set(["HI"], mergedHints);
      return `HI ${mergedHints.length} hints, official dungeon IDs retained (from ${hints.split("/").slice(-1)[0]})${edits(Object.keys(state?.hints?.entries ?? {}).length)}`;
    }, 2);
  }
  writeFileSync(join(out, `versions_${lang}.txt`), txt);
}
writeFileSync(join(out, "versions.swf"), index.toBuffer());
