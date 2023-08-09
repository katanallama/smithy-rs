/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

//! Module to extend the functionality of `FnStream` to allow for collecting elements of the stream
//! into collection.
//!
//! Majority of the code is borrowed from
//! <https://github.com/tokio-rs/tokio/blob/fc9518b62714daac9a38b46c698b94ac5d5b1ca2/tokio-stream/src/stream_ext/collect.rs>

/// A trait that signifies that elements can be collected into `T`.
///
/// Currently the trait may not be implemented by clients so we can make changes in the future
/// without breaking code depending on it.
pub trait Collectable<T>: sealed::CollectablePrivate<T> {}

pub(crate) mod sealed {
    #[doc(hidden)]
    pub trait CollectablePrivate<T> {
        type Collection;

        fn initialize() -> Self::Collection;

        fn extend(collection: &mut Self::Collection, item: T) -> bool;

        fn finalize(collection: &mut Self::Collection) -> Self;
    }
}

impl<T> Collectable<T> for Vec<T> {}

impl<T> sealed::CollectablePrivate<T> for Vec<T> {
    type Collection = Self;

    fn initialize() -> Self::Collection {
        Vec::default()
    }

    fn extend(collection: &mut Self::Collection, item: T) -> bool {
        collection.push(item);
        true
    }

    fn finalize(collection: &mut Self::Collection) -> Self {
        std::mem::take(collection)
    }
}

impl<T, U, E> Collectable<Result<T, E>> for Result<U, E> where U: Collectable<T> {}

impl<T, U, E> sealed::CollectablePrivate<Result<T, E>> for Result<U, E>
where
    U: Collectable<T>,
{
    type Collection = Result<U::Collection, E>;

    fn initialize() -> Self::Collection {
        Ok(U::initialize())
    }

    fn extend(collection: &mut Self::Collection, item: Result<T, E>) -> bool {
        match item {
            Ok(item) => {
                let collection = collection.as_mut().ok().expect("invalid state");
                U::extend(collection, item)
            }
            Err(e) => {
                *collection = Err(e);
                false
            }
        }
    }

    fn finalize(collection: &mut Self::Collection) -> Self {
        if let Ok(collection) = collection.as_mut() {
            Ok(U::finalize(collection))
        } else {
            let res = std::mem::replace(collection, Ok(U::initialize()));
            Err(res.map(drop).unwrap_err())
        }
    }
}
