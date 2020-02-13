import cnames.structs.RedisModuleCtx
import cnames.structs.RedisModuleIO
import cnames.structs.RedisModuleString
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import redismodule.Brackets_EmitAOF
import redismodule.REDISMODULE_ERRORMSG_WRONGTYPE
import redismodule.REDISMODULE_KEYTYPE_EMPTY
import redismodule.REDISMODULE_OK
import redismodule.REDISMODULE_READ
import redismodule.REDISMODULE_WRITE
import redismodule.RedisModule_CloseKey
import redismodule.RedisModule_KeyType
import redismodule.RedisModule_LoadStringBuffer
import redismodule.RedisModule_ModuleTypeGetType
import redismodule.RedisModule_ModuleTypeGetValue
import redismodule.RedisModule_ModuleTypeSetValue
import redismodule.RedisModule_OpenKey
import redismodule.RedisModule_ReplicateVerbatim
import redismodule.RedisModule_ReplyWithError
import redismodule.RedisModule_ReplyWithSimpleString
import redismodule.RedisModule_SaveStringBuffer
import redismodule.RedisModule_StringPtrLen
import redismodule.RedisModule_WrongArity

class Bracket(val prev: Bracket?, val symbol: Char)

class Brackets {
    private var head: Bracket? = null

    fun push(symbol: Char) {
        head = if (
                ((symbol == ')') && (head?.symbol == '(')) ||
                ((symbol == ']') && (head?.symbol == '[')) ||
                ((symbol == '}') && (head?.symbol == '{'))
        ) {
            head?.prev
        } else {
            Bracket(head, symbol)
        }
    }

    val valid: Boolean
        get() = (head == null)

    override fun toString(): String {
        fun visit(b: Bracket, buf: String): String {
            return if (b.prev != null) {
                visit(b.prev, b.symbol + buf)
            } else {
                b.symbol + buf
            }
        }

        return this.head?.let {
            visit(it, "")
        } ?: ""
    }
}

fun bracketsKnPush(
        ctx: CPointer<RedisModuleCtx>?,
        argv: CPointer<CPointerVar<RedisModuleString>>?,
        argc: Int
): Int {
    println("bracketsKnPush")

    if (argc != 3) {
        return (RedisModule_WrongArity!!)(ctx)
    }

    if (argv == null) {
        memScoped {
            return (RedisModule_ReplyWithError!!)(ctx, "argv is null".cstr.ptr)
        }
    }

    val bracket = memScoped {
        (RedisModule_StringPtrLen!!)(argv[2], alloc<ULongVar>().ptr)?.toKString()?.get(0) ?: ' '
    }

    if (bracket !in listOf('(', ')', '{', '}', '[', ']')) {
        memScoped {
            return (RedisModule_ReplyWithError!!)(ctx, "Please, push only one of the `(`, `)`, `{`, `}`, `[`, `]` symbols".cstr.ptr)
        }
    }

    val key = (RedisModule_OpenKey!!)(ctx, argv[1], REDISMODULE_READ or REDISMODULE_WRITE)?.reinterpret<cnames.structs.RedisModuleKey>()
    val type = (RedisModule_KeyType!!)(key)

    if ((type != REDISMODULE_KEYTYPE_EMPTY) && ((RedisModule_ModuleTypeGetType!!)(key) != KNBracketType)) {
        memScoped {
            return (RedisModule_ReplyWithError!!)(ctx, REDISMODULE_ERRORMSG_WRONGTYPE.cstr.ptr)
        }
    }

    if (type == REDISMODULE_KEYTYPE_EMPTY) {
        val obj = Brackets()

        obj.push(bracket)

        (RedisModule_ModuleTypeSetValue!!)(key, KNBracketType, StableRef.create(obj).asCPointer())
    } else {
        (RedisModule_ModuleTypeGetValue!!)(key)?.asStableRef<Brackets>()?.let { ref ->
            ref.get().push(bracket)
        }
    }

    memScoped {
        (RedisModule_ReplyWithSimpleString!!)(ctx, "OK".cstr.ptr)
    }

    (RedisModule_CloseKey!!)(key)
    (RedisModule_ReplicateVerbatim!!)(ctx)

    return REDISMODULE_OK
}

fun bracketsKnPrint(
        ctx: CPointer<RedisModuleCtx>?,
        argv: CPointer<CPointerVar<RedisModuleString>>?,
        argc: Int
): Int {
    println("bracketsKnPrint")

    if (argc != 2) {
        return (RedisModule_WrongArity!!)(ctx)
    }

    if (argv == null) {
        memScoped {
            return (RedisModule_ReplyWithError!!)(ctx, "argv is null".cstr.ptr)
        }
    }

    val key = (RedisModule_OpenKey!!)(ctx, argv[1], REDISMODULE_READ)?.reinterpret<cnames.structs.RedisModuleKey>()
    val type = (RedisModule_KeyType!!)(key)

    if ((type != REDISMODULE_KEYTYPE_EMPTY) && ((RedisModule_ModuleTypeGetType!!)(key) != KNBracketType)) {
        memScoped {
            return (RedisModule_ReplyWithError!!)(ctx, REDISMODULE_ERRORMSG_WRONGTYPE.cstr.ptr)
        }
    }

    memScoped {
        (RedisModule_ReplyWithSimpleString!!)(
                ctx,
                ((RedisModule_ModuleTypeGetValue!!)(key)?.asStableRef<Brackets>()?.get()?.toString() ?: "").cstr.ptr
        )
    }

    return REDISMODULE_OK
}

fun bracketsKnValid(
        ctx: CPointer<RedisModuleCtx>?,
        argv: CPointer<CPointerVar<RedisModuleString>>?,
        argc: Int
): Int {
    println("bracketsKnValid")

    if (argc != 2) {
        return (RedisModule_WrongArity!!)(ctx)
    }

    if (argv == null) {
        memScoped {
            return (RedisModule_ReplyWithError!!)(ctx, "argv is null".cstr.ptr)
        }
    }

    val key = (RedisModule_OpenKey!!)(ctx, argv[1], REDISMODULE_READ)?.reinterpret<cnames.structs.RedisModuleKey>()
    val type = (RedisModule_KeyType!!)(key)

    if ((type != REDISMODULE_KEYTYPE_EMPTY) && ((RedisModule_ModuleTypeGetType!!)(key) != KNBracketType)) {
        memScoped {
            return (RedisModule_ReplyWithError!!)(ctx, REDISMODULE_ERRORMSG_WRONGTYPE.cstr.ptr)
        }
    }

    memScoped {
        (RedisModule_ReplyWithSimpleString!!)(
                ctx,
                ((RedisModule_ModuleTypeGetValue!!)(key)?.asStableRef<Brackets>()?.get()?.valid ?: false).toString().cstr.ptr
        )
    }

    return REDISMODULE_OK
}

fun bracketsRdbLoad(
        rdb: CPointer<RedisModuleIO>?,
        encver: Int
): COpaquePointer? {
    println("bracketsRdbLoad")

    if (encver != BRACKETS_KN_VERSION) {
        println("Cannot load version $encver")

        return null
    }

    val value = memScoped {
        val value = (RedisModule_LoadStringBuffer!!)(rdb, alloc<ULongVar>().ptr)

        value?.toKString() ?: ""
    }

    val obj = Brackets()

    value.forEach {
        obj.push(it)
    }

    return StableRef.create(obj).asCPointer()
}

@ExperimentalUnsignedTypes
fun bracketsRdbSave(
        rdb: CPointer<RedisModuleIO>?,
        value: COpaquePointer?
) {
    println("bracketsRdbSave")

    value?.asStableRef<Brackets>()?.get()?.let {
        memScoped {
            val str = it.toString().cstr

            (RedisModule_SaveStringBuffer!!)(rdb, str.ptr, str.size.toULong())
        }
    }
}

fun bracketsAofRewrite(
        aof: CPointer<RedisModuleIO>?,
        key: CPointer<RedisModuleString>?,
        value: COpaquePointer?
) {
    println("bracketsAofRewrite")

    value?.asStableRef<Brackets>()?.get()?.let {
        it.toString().forEach { bracket ->
            Brackets_EmitAOF(aof, key, "$bracket".cstr)
        }
    }
}

fun bracketsFree(value: COpaquePointer?) {
    println("bracketsFree")

    value?.asStableRef<Brackets>()?.dispose()
}
